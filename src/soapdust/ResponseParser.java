package soapdust;

import static soapdust.SoapDustNameSpaceContext.SOAPENV;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResponseParser {

	ComposedValue parse(InputStream inputStream, byte[] trace) throws IOException, MalformedResponseException {
		try {
			Node soapBody = soapNode(inputStream, "/" + SOAPENV + ":Envelope/" + SOAPENV + ":Body");
			Object parseResponseBody = parse(soapBody);
			resolvePendingChildren();
			return (ComposedValue) parseResponseBody;
		} catch (SAXException e) {
			throw new MalformedResponseException("Server returned a malformed response: " + e, 200, trace);
		}
	}
	
	FaultResponseException parseFault(InputStream inputStream, int responseCode, byte[] trace) throws MalformedResponseException, IOException {
		try {
			Node fault = soapNode(inputStream, SOAPENV + ":Envelope/" + SOAPENV + ":Body/" + SOAPENV + ":Fault");

			ComposedValue result = (ComposedValue) parse(fault);
			String message = result.getChildrenKeys().contains("faultstring") ? result.getStringValue("faultstring") : null;

			return new FaultResponseException(message, result, responseCode);
		} catch (SAXException e) {
			throw new MalformedResponseException("Server returned a malformed response: " + e, responseCode, trace);
		}
	}
	
	private Object parse(Node node) {
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
				Object childValue = parse(child);
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

	private String attribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute != null ? attribute.getNodeValue() : "";
	}
	
	private Node soapNode(InputStream inputStream, String nodePath) throws IOException, SAXException {

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
	
	private DocumentBuilder newXmlParser() {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);	
			DocumentBuilder parser;
			parser = documentBuilderFactory.newDocumentBuilder();
			return parser;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("unexpected exception: " + e, e);
		}
	}
	
	private XPath newXPath() {
		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		xpath.setNamespaceContext(new SoapDustNameSpaceContext());
		return xpath;
	}

	//---
	//FIXME this method is duplicated in Client
	private byte[] inputToBytes(InputStream in)
	throws IOException {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		
		for(int read = in.read(buffer, 0, buffer.length); read != -1; read = in.read(buffer, 0, buffer.length)) {
			content.write(buffer, 0, read);
		}

		return content.toByteArray();
	}
}
