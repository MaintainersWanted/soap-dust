package soapdust;

import static soapdust.SoapDustNameSpaceContext.SOAP;
import static soapdust.SoapDustNameSpaceContext.WSDL;
import static soapdust.SoapDustNameSpaceContext.XSD;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

class WsdlParser {
	
	//FIXME this class is utter crap ! One should rewrite it from scratch to be more xsd/wsdl/soap compliant
	
	static ServiceDescription parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = newXmlParser();

		Document document = parser.parse(inputStream);

		SoapDustNameSpaceContext nameSpaceContext = new SoapDustNameSpaceContext();
		XPath xpath = newXPath(nameSpaceContext);
		Node definitions = (Node) xpath.compile("/" + WSDL + ":definitions").evaluate(document, XPathConstants.NODE);
		addXmlNs(nameSpaceContext, definitions);
		NodeList operations = (NodeList) xpath.compile(WSDL + ":portType/" + WSDL + ":operation").evaluate(definitions, XPathConstants.NODESET);

		ServiceDescription serviceDescription = new ServiceDescription();
		
		String definitionsTargetNamespace = attribute(definitions, "targetNamespace");
		
		serviceDescription.operations.put("*", new WsdlOperation("", definitionsTargetNamespace)); //default operation
		for (int i = 0; i < operations.getLength(); i++) {
			Node operationNode = operations.item(i);
			WsdlOperation operation = new WsdlOperation(soapActionFor(xpath, definitions, operationNode), definitionsTargetNamespace);
			operation.setStyle(soapOperationStyleFor(xpath, definitions, operationNode));
			serviceDescription.operations.put(attribute(operationNode, "name"), operation);
			addParameters(operation, xpath, nameSpaceContext, definitions, operationNode, definitionsTargetNamespace, operation.parts);
		}
		return serviceDescription;
	}
	
	private static String soapActionFor(XPath xpath, Node definitions, Node operationNode) throws XPathExpressionException {
		String operationName = attribute(operationNode, "name");
		String expression = WSDL + ":binding/" + WSDL + ":operation[@name='" + operationName  + "']/" + SOAP + ":operation";
		Node soapAction = (Node) xpath.compile(expression).evaluate(definitions, XPathConstants.NODE);
		if (soapAction == null) {
			//no soap action specified in wsdl
			return null;
		} else {
			return attribute(soapAction, "soapAction");
		}
	}

	private static String soapOperationStyleFor(XPath xpath, Node definitions, Node operationNode) throws XPathExpressionException {
		String style = attribute(operationNode, "style");
		if (style == null || style.equals("")) {
			String expression = WSDL + ":binding/" + SOAP + ":binding";
			Node binding = (Node) xpath.compile(expression).evaluate(definitions, XPathConstants.NODE);
			style = attribute(binding, "style");
		}
		return style;
	}


	private static void addParameters(WsdlOperation operation, XPath xpath, SoapDustNameSpaceContext nameSpaceContext, Node definitions,
			Node operationNode, String namespace, Map<String, WsdlElement> parent) throws XPathExpressionException {

		Node input = (Node) xpath.compile(WSDL + ":input").evaluate(operationNode, XPathConstants.NODE);
		if (input == null) {
			//this soap operation has no input, ignoring...
			return;
		}
		String messageName = attribute(input, "message").replaceAll(".*:", "");
		Node message = (Node) xpath.compile(WSDL + ":message[@name='" + messageName + "']").evaluate(definitions, XPathConstants.NODE);
		NodeList params = (NodeList) xpath.compile(WSDL + ":part").evaluate(message, XPathConstants.NODESET);

		addParameters(operation, parent, namespace, params, xpath, definitions, nameSpaceContext, nameSpaceContext, true);
	}
	
	private static void addParameters(WsdlOperation operation, Map<String, WsdlElement> parent, String namespace, NodeList parameterNodes, XPath xpath, Node definitions, 
			SoapDustNameSpaceContext localNameSpaceContext, SoapDustNameSpaceContext globalNameSpaceContext, boolean messagePart) throws XPathExpressionException {
		
		for(int i = 0; i < parameterNodes.getLength(); i++) {
			Node parameterNode = parameterNodes.item(i);
			String parameterName = attribute(parameterNode, "name");
			WsdlElement parameter = new WsdlElement(namespace);
			if ((operation.getStyle() == WsdlOperation.DOCUMENT) && messagePart) {
				String parameterType = attribute(parameterNode, "type");
				if (parameterType == null || parameterType.equals("")) parameterType = attribute(parameterNode, "element");
				parameterType = parameterType.substring(parameterType.indexOf(":") + 1);
				parent.put(parameterType, parameter);
			} else {
				parent.put(parameterName, parameter);
			}

			addSubParameters(operation, namespace, xpath, definitions, localNameSpaceContext,
					globalNameSpaceContext, messagePart, parameterNode,
					parameter);
		}
	}

	private static void addSubParameters(WsdlOperation operation,
			String namespace,
			XPath xpath, Node definitions,
			SoapDustNameSpaceContext localNameSpaceContext,
			SoapDustNameSpaceContext globalNameSpaceContext,
			boolean messagePart, Node parameterNode, WsdlElement parameter)
			throws XPathExpressionException {
		
		String parameterType = typeOrElementAttribute(parameterNode);
		String parameterTypeNamespace = namespace;
		if(parameterType.lastIndexOf(":") != -1) {
			String[] split = parameterType.split(":");
			parameterTypeNamespace = localNameSpaceContext.getNamespaceURI(split[0]);
			parameterType = split[1];
		}

		Node schema = (Node) xpath.compile(WSDL + ":types/" + XSD + ":schema[@targetNamespace='" + parameterTypeNamespace + "']").evaluate(definitions, XPathConstants.NODE);
		if (schema == null) {return;}
		
		SoapDustNameSpaceContext parameterTypeNSContext = new SoapDustNameSpaceContext(globalNameSpaceContext);
		addXmlNs(parameterTypeNSContext, schema);

		Node type = (Node) xpath.compile("./*[@name='" + parameterType + "']").evaluate(schema, XPathConstants.NODE);
		if (type != null) {
			String typeType = attribute(type, "type");
			if ("".equals(typeType)) {//is it possible for a complexType or a simpleType to have an attribute type ?
				NodeList subParameters = (NodeList) xpath.compile(".//" + XSD + ":element").evaluate(type, XPathConstants.NODESET);
				addParameters(operation, parameter.children, parameterTypeNamespace, subParameters, xpath, definitions, parameterTypeNSContext, globalNameSpaceContext, false);
			} else {
				addSubParameters(operation, namespace, xpath, definitions, localNameSpaceContext,
						globalNameSpaceContext, messagePart, type,
						parameter);
			}
		}
	}

	private static void addXmlNs(SoapDustNameSpaceContext nameSpaceContext,
			Node node) {
		NamedNodeMap attributes = node.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			String attributeName = attribute.getNodeName();
			if (attributeName.startsWith("xmlns:")) {
				String prefix = attributeName.substring("xmlns:".length());
				String uri = attribute.getNodeValue();
				nameSpaceContext.addNamespace(prefix, uri);
			}
		}
	}

	private static DocumentBuilder newXmlParser() throws ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);	
		DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
		return parser;
	}
	
	private static String attribute(Node node, String attributeName) {
		Node attribute = node.getAttributes().getNamedItem(attributeName);
		return attribute != null ? attribute.getNodeValue() : "";
	}
	
	
	private static String typeOrElementAttribute(Node node) {
		NamedNodeMap attributes = node.getAttributes();
		Node type = attributes.getNamedItem("type");
		Node element = attributes.getNamedItem("element");
		if (type != null) {
			return type.getNodeValue();
		} if (element != null) {
			return element.getNodeValue();
		} else {
			return "";
		}
	}

	private static XPath newXPath(SoapDustNameSpaceContext nsContext) {
		javax.xml.xpath.XPathFactory factory = javax.xml.xpath.XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		xpath.setNamespaceContext(nsContext);
		return xpath;
	}
}

class SoapDustNameSpaceContext implements NamespaceContext {

	public static final String SOAP = "_soap_";
	public static final String SOAPENV = "_soapenv_";
	public static final String XSD = "_xsd_";
	public static final String WSDL = "_wsdl_";

	private Map<String, String> nameSpaceURIS;

	public SoapDustNameSpaceContext() {
		nameSpaceURIS = new HashMap<String, String>();
		nameSpaceURIS.put(SOAP, "http://schemas.xmlsoap.org/wsdl/soap/");
		nameSpaceURIS.put(WSDL, "http://schemas.xmlsoap.org/wsdl/");
		nameSpaceURIS.put(XSD, "http://www.w3.org/2001/XMLSchema");
		nameSpaceURIS.put(SOAPENV, "http://schemas.xmlsoap.org/soap/envelope/");
	}

	public SoapDustNameSpaceContext(SoapDustNameSpaceContext parent) {
		this.nameSpaceURIS = new HashMap<String, String>(parent.nameSpaceURIS);
	}
	
	public String getNamespaceURI(String prefix) {
		return nameSpaceURIS.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		throw new UnsupportedOperationException();
	}

	public Iterator getPrefixes(String namespaceURI) {
		throw new UnsupportedOperationException();
	}
	
	public void addNamespace(String prefix, String uri) {
		nameSpaceURIS.put(prefix, uri);
	}
}