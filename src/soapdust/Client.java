package soapdust;

import static soapdust.SoapDustNameSpaceContext.SOAPENV;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import sun.misc.BASE64Encoder;

public class Client {

	private ServiceDescription serviceDescription;
	private URL endPointUrl;
	private String password;
	private String userName;
	private URL wsdlUrl;
	private boolean debug;
	private byte[] lastInput;
	private Map<String, List<String>> lastReceivedHeaders;

	public Client(boolean debug) {
		this();
		this.debug = debug;
	}

	public Client() {
	}

	public void explain(OutputStream out) throws IOException {
		explain(new OutputStreamWriter(out));
	}
	
	public void explain(Writer out) throws IOException {
		if (this.wsdlUrl == null) throw new IllegalStateException("you must set a wsdl url to get an explanation...");
		
		BufferedWriter bout = new BufferedWriter(out);
		for (Entry<String, WsdlOperation> entry : serviceDescription.operations.entrySet()) {
			if ("*".equals(entry.getKey())) continue;
			bout.write(entry.getKey());

			printTree(bout, "\t", entry.getValue().parts);
			
			bout.newLine();
		}
		bout.flush();
	}

	private String printTree(BufferedWriter bout, String indentation, Map<String, WsdlElement> type) throws IOException {
		for (Entry<String, WsdlElement> messageEntry : type.entrySet()) {
			bout.newLine();
			bout.write(indentation);
			bout.write(messageEntry.getKey());
			printTree(bout, indentation + "\t", messageEntry.getValue().children);
		}
		return indentation;
	}
	
	public ComposedValue call(String operation, ComposedValue parameters) throws FaultResponseException, IOException, MalformedResponseException {
		Document message;
		try {
			message = createMessage(operation, parameters);
		} catch (ParserConfigurationException e) {
			String msg = "Unexpected exception while preparing soap request: " + e;
			throw new RuntimeException(msg, e);
		}
		HttpURLConnection connection = initHttpConnection();
		addSoapAction(connection, operation);
		try {
			try {
				sendRequest(message, connection);
			} catch (TransformerException e) {
				throw new RuntimeException("Unexpected exception while sending soap request to server: " + e, e);
			}
			try {
				return readResponse(connection);
			} catch (SAXException e) {
				throw malformedResponseException(e);
			} catch (ParserConfigurationException e) {
				throw malformedResponseException(e);
			} 
		} finally {
			connection.disconnect();
		}
	}

	private void addSoapAction(HttpURLConnection connection, String operation) {
		WsdlOperation wsdlOperation = serviceDescription.operations.get(operation);
		if (wsdlOperation != null && wsdlOperation.soapAction != null) {
			connection.addRequestProperty("SOAPAction", wsdlOperation.soapAction);
		}
	}

	private MalformedResponseException malformedResponseException(Exception e) throws MalformedResponseException {
		String msg = "Server returned a malformed response: " + e;
		if (debug) {
			msg += "received headers: " + lastReceivedHeaders;
			msg += "\n";
			BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(lastInput)));
			msg += "\n";
			try {
				for(String line = reader.readLine(); line != null; line = reader.readLine()) {
					msg += line + "\n";
				}
			} catch (IOException unexpectedException) {
				throw new RuntimeException("IOException while reading data from a byte array ????", unexpectedException);
			}
		}
		return new MalformedResponseException(msg, e);
	}

	private Document createMessage(String operation, ComposedValue parameters) throws ParserConfigurationException {

		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

		Document document = documentBuilder.newDocument();

		Element operationElement = createOperationElement(operation, document);
		WsdlElement operationWsdlElement = serviceDescription.messages.get(operation);
		Map<String, WsdlElement> children = serviceDescription.operations.get(operation).parts;
		addParameters(document, operationElement, parameters, children, operationWsdlElement.namespace);
		return document;
	}

	public ComposedValue call(String operation) throws FaultResponseException, IOException, MalformedResponseException  {
		return call(operation, new ComposedValue());
	}

	/**
	 * Override this method if you want to customize the http connection.
	 * For instance you may add http headers like SOAPAction...
	 * @param connection the HttpURLConnection to customize
	 */
	protected void customizeHttpConnectionBeforeCall(HttpURLConnection connection) {

	}

	public void setWsdlUrl(String wsdlUrl) throws IOException, MalformedWsdlException {
		try {
			this.wsdlUrl = new URL(wsdlUrl);
			serviceDescription = WsdlParser.parse(this.wsdlUrl.openStream());
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unexpected exception while \"analysing\" wsdl: " + e, e);
		} catch (XPathExpressionException e) {
			throw new RuntimeException("Unexpected exception while \"analysing\" wsdl: " + e, e);
		} catch (SAXException e) {
			throw new MalformedWsdlException("Unable to \"analyse\" the specified wsdl: " + e, e);
		}
	}

	private DocumentBuilder newXmlParser() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);	
		DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
		return parser;
	}

	public void setUsername(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEndPoint(String url) throws MalformedURLException {
		this.endPointUrl = new URL(url);
	}


	//----

	private Element createOperationElement(String operation, Document document) {

		Element envelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		Element header = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
		Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");

		WsdlElement operationWsdlElement = serviceDescription.messages.get(operation);

		document.appendChild(envelope);
		envelope.appendChild(header);
		envelope.appendChild(body);

		switch (serviceDescription.operations.get(operation).getStyle()) {
		case WsdlOperation.RPC:
			Element operationElement = document.createElementNS(operationWsdlElement.namespace, operation);
			body.appendChild(operationElement);
			return operationElement;
			
		case WsdlOperation.DOCUMENT:
			default:
			return body;
		}
	}			


	ComposedValue readResponse(HttpURLConnection connection) 
	throws FaultResponseException, IOException, SAXException, ParserConfigurationException, MalformedResponseException {

		InputStream inputStream;
		try {
			if (debug) {
				lastReceivedHeaders = connection.getHeaderFields();
			}
			inputStream = inputStream(connection);
		} catch (IOException e) {
			int responseCode = responseCode(connection);
			if (responseCode != 200 && responseCode != -1) {
				throw createFaultException(connection);
			} else {
				throw e;
			}
		}
		if (debug) {
			inputStream = saveInput(inputStream);
		}
		handleResponseCode(connection);
		return parseResponseBody(inputStream);
	}

	void handleResponseCode(HttpURLConnection connection) throws IOException,
	MalformedResponseException {
		int responseCode = responseCode(connection);
		String errorMessage = "unsupported HTTP response code " + responseCode;
		switch (responseCode) {
		case 200:
			return;
		case 302:
			errorMessage = "unsupported HTTP response code " + responseCode 
			+ " Location: " + connection.getHeaderField("Location");
		default:
			byte[] data = inputToBytes(inputStream(connection));
			throw new MalformedResponseException(errorMessage, responseCode, data);
		}
	}

	private InputStream saveInput(InputStream inputStream) throws IOException {
		lastInput = inputToBytes(inputStream);
		return new ByteArrayInputStream(lastInput);
	}

	private byte[] inputToBytes(InputStream inputStream)
	throws IOException {
		ByteArrayOutputStream save = new ByteArrayOutputStream();
		byte[] b = new byte[1024];
		int nbRead = inputStream.read(b);
		while (nbRead != -1) {
			save.write(b, 0, nbRead);
			nbRead = inputStream.read(b);
		}
		return save.toByteArray();
	}

	FaultResponseException createFaultException(HttpURLConnection connection) throws IOException, SAXException, ParserConfigurationException {
		InputStream errorStream = errorStream(connection);
		int responseCode = responseCode(connection);
		byte[] data = inputToBytes(errorStream);
		errorStream.close();
		Node fault = soapNode(new ByteArrayInputStream(data), SOAPENV + ":Envelope/" + SOAPENV + ":Body/" + SOAPENV + ":Fault");

		ComposedValue result = (ComposedValue) parseResponseBody(fault);
		String message = result.getChildrenKeys().contains("faultstring") ? result.getStringValue("faultstring") : null;

		return new FaultResponseException(message, result, data, responseCode);
	}

	private void sendRequest(Document message, HttpURLConnection connection)
	throws IOException, TransformerException {

		OutputStream out = outputStream(connection);
		Transformer transformer = TransformerFactory.newInstance().newTransformer();

		transformer.transform(new DOMSource(message), new StreamResult(out));
		out.flush();
		out.close();
	}

	/**
	 * Package visibility for unit tests only.
	 */ 
	OutputStream outputStream(HttpURLConnection connection)
	throws IOException {
		OutputStream out = connection.getOutputStream();
		return out;
	}

	/**
	 * Package visibility for unit tests only.
	 */
	InputStream inputStream(HttpURLConnection connection)
	throws IOException {
		InputStream inputStream;
		inputStream = connection.getInputStream();
		return inputStream;
	}

	/**
	 * Package visibility for unit tests only.
	 */
	InputStream errorStream(HttpURLConnection connection)
	throws IOException {
		InputStream inputStream;
		inputStream = connection.getErrorStream();
		return inputStream;
	}

	/**
	 * Package visibility for unit tests only.
	 */
	int responseCode(HttpURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		return responseCode;
	}





	private HttpURLConnection initHttpConnection() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) endPointUrl.openConnection();
		addAuthenticationIfNeeded(connection);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
		connection.setRequestProperty("Accept", "*/*");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		customizeHttpConnectionBeforeCall(connection);

		return connection;
	}

	private void addAuthenticationIfNeeded(HttpURLConnection connection) {
		if (userName == null && password == null) {
			return;
		}
		if (userName != null && password == null) {
			throw new NullPointerException("userName is not null: password can not be null");
		}
		if (password != null && userName == null) {
			throw new NullPointerException("password is not null: userName can not be null");
		}
		String authenticationString = new BASE64Encoder().encode((userName + ":" + password).getBytes());
		connection.setRequestProperty("Authorization", "Basic " + authenticationString);
	}

	private void addParameters(Document document, Element operationElement, ComposedValue parameters, Map<String, WsdlElement> parent, String defaultNamespace) {

		for (String childKey : parameters.getChildrenKeys()) {

			WsdlElement paramWsdlElement = parent.get(childKey);
			String namespace = paramWsdlElement != null ? paramWsdlElement.namespace : defaultNamespace;

			Element param = document.createElementNS(namespace, childKey);
			operationElement.appendChild(param);

			Object childValue = parameters.getValue(childKey);
			if (childValue instanceof String) {
				Text value = document.createTextNode((String) childValue);
				param.appendChild(value);
			} else if (childValue instanceof ComposedValue) {
				addParameters(document, param, (ComposedValue) childValue, paramWsdlElement == null ? parent : paramWsdlElement.children, namespace);
			} else {
				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
			}
		}
	}

	ComposedValue parseResponseBody(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
		Node soapBody = soapNode(inputStream, "/" + SOAPENV + ":Envelope/" + SOAPENV + ":Body");
		Object parseResponseBody = parseResponseBody(soapBody);
		resolvePendingChildren();
		return (ComposedValue) parseResponseBody;
	}

	private Node soapNode(InputStream inputStream, String nodePath) throws IOException, SAXException, ParserConfigurationException {

		try {
			DocumentBuilder builder = newXmlParser();
			Document document = builder.parse(inputStream);

			XPath xpath = newXPath();

			XPathExpression expr;
			expr = xpath.compile(nodePath);
			Node soapBody = (Node) expr.evaluate(document, XPathConstants.NODE);

			return soapBody;
		} catch (XPathExpressionException e) {
			throw new RuntimeException("unexpected exception: " + e, e);
		}
	}

	private Object parseResponseBody(Node node) {
		{
			Node child = node.getFirstChild();
			if (child != null && child.getNodeType() == Node.TEXT_NODE) {
				// either this node is a string and child is the value
				// => we return the string
				int nbChildren = node.getChildNodes().getLength();
				if (nbChildren == 1) {
					return child.getNodeValue();
				}
				// or this child is just indentation
				// => we ignore it
			}
		}

		ComposedValue thisNodeResult = new ComposedValue();

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node child = childNodes.item(i);
			// otherwise we assume we have several SoapElement children
			// ignoring text children
			if (child.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			String childLocalName = child.getLocalName();
			String href = attribute(child, "href");
			String id = attribute(child, "id");
			if ("".equals(href)) {
				Object childValue = parseResponseBody(child);
				thisNodeResult.put(childLocalName, childValue);
				if (! "".equals(id)) {
					addIdentifiedNode(id, childValue);
				}
			} else {
				addPendingChild(thisNodeResult, childLocalName, href);
			}
		}
		return thisNodeResult;
	}

	private Map<ComposedValue, Map<String, String>> pendingChildren = new HashMap<ComposedValue, Map<String, String>>();
	private void addPendingChild(ComposedValue parent, String childKey, String hrefId) {
		Map<String, String> parentPendingChildren = pendingChildren.get(parent);
		if (parentPendingChildren == null) {
			parentPendingChildren = new HashMap<String, String>();
			pendingChildren.put(parent, parentPendingChildren);
		}
		String childRefId = parentPendingChildren.get(childKey);
		if (childRefId == null) {
			parentPendingChildren.put(childKey, hrefId);
		} else {
			//TODO this sucks !!! Find a better way to represent object with several children having the same name.
			//     see also ComposedValue TODO
			int i = 1;
			for(; parentPendingChildren.get(childKey + i) != null; i++);
			parentPendingChildren.put(childKey + i, hrefId);
		}
	}
	private Map<String, Object> identifiedNodes = new HashMap<String, Object>();
	private void addIdentifiedNode(String id, Object value) {
		identifiedNodes.put("#" + id, value);
	}
	private void resolvePendingChildren() {
		for (Entry<ComposedValue, Map<String, String>> entry : pendingChildren.entrySet()) {
			ComposedValue parent = entry.getKey();
			for (Entry<String, String> children : entry.getValue().entrySet()) {
				String childKey = children.getKey();
				String childHrefId = children.getValue();
				parent.put(childKey, identifiedNodes.get(childHrefId));
			}
		}
		pendingChildren = new HashMap<ComposedValue, Map<String, String>>();
		identifiedNodes = new HashMap<String, Object>();
	}

	private XPath newXPath() {
		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		xpath.setNamespaceContext(new SoapDustNameSpaceContext());
		return xpath;
	}

	private String attribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute != null ? attribute.getNodeValue() : "";
	}

}