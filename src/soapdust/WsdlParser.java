package soapdust;

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

public class WsdlParser {

	public static Map<String, WsdlElement> parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		DocumentBuilder parser = newXmlParser();

		Document document = parser.parse(inputStream);

		SoapDustNameSpaceContext nameSpaceContext = new SoapDustNameSpaceContext();
		XPath xpath = newXPath(nameSpaceContext);
		Node definitions = (Node) xpath.compile("/" + WSDL + ":definitions").evaluate(document, XPathConstants.NODE);
		addXmlNs(nameSpaceContext, definitions);
		NodeList operations = (NodeList) xpath.compile(WSDL + ":portType/" + WSDL + ":operation").evaluate(definitions, XPathConstants.NODESET);

		HashMap<String, WsdlElement> result = new HashMap<String, WsdlElement>();
		String definitionsTargetNamespace = attribute(definitions, "targetNamespace");
		result.put("*", new WsdlElement(definitionsTargetNamespace)); //default namespace
		for (int i = 0; i < operations.getLength(); i++) {
			Node operationNode = operations.item(i);
			addParameters(xpath, nameSpaceContext, definitions, operationNode, definitionsTargetNamespace, result);
		}
		return result;
	}
	
	private static void addParameters(XPath xpath, SoapDustNameSpaceContext nameSpaceContext, Node definitions,
			Node operationNode, String namespace, Map<String, WsdlElement> parent) throws XPathExpressionException {

		Node input = (Node) xpath.compile(WSDL + ":input").evaluate(operationNode, XPathConstants.NODE);
		String messageName = attribute(input, "message").replaceAll(".*:", "");
		String expression = WSDL + ":message[@name='" + messageName + "']";
		Node message = (Node) xpath.compile(expression).evaluate(definitions, XPathConstants.NODE);
		NodeList params = (NodeList) xpath.compile(WSDL + ":part").evaluate(message, XPathConstants.NODESET);

		addParameters(parent, namespace, params, xpath, definitions, nameSpaceContext, nameSpaceContext);
	}
	
	private static void addParameters(Map<String, WsdlElement> parent, String namespace, NodeList parameterNodes, XPath xpath, Node definitions, 
			SoapDustNameSpaceContext localNameSpaceContext, SoapDustNameSpaceContext globalNameSpaceContext) throws XPathExpressionException {
		
		for(int i = 0; i < parameterNodes.getLength(); i++) {
			Node parameterNode = parameterNodes.item(i);
			String parameterName = attribute(parameterNode, "name");
			WsdlElement parameter = new WsdlElement(namespace);
			parent.put(parameterName, parameter);

			String parameterType = typeOrElementAttribute(parameterNode);
			String parameterTypeNamespace = namespace;
			if(parameterType.lastIndexOf(":") != -1) {
				String[] split = parameterType.split(":");
				parameterTypeNamespace = localNameSpaceContext.getNamespaceURI(split[0]);
				parameterType = split[1];
			}

			Node schema = (Node) xpath.compile(WSDL + ":types/" + XSD + ":schema[@targetNamespace='" + parameterTypeNamespace + "']").evaluate(definitions, XPathConstants.NODE);
			if (schema == null) {continue;}
			
			SoapDustNameSpaceContext parameterTypeNSContext = new SoapDustNameSpaceContext(globalNameSpaceContext);
			addXmlNs(parameterTypeNSContext, schema);
			Node type = (Node) xpath.compile(".//*[@name='" + parameterType + "']").evaluate(schema, XPathConstants.NODE);
			if (type == null) {continue;}
			
			NodeList subParameters = (NodeList) xpath.compile(".//" + XSD + ":element").evaluate(type, XPathConstants.NODESET);
			addParameters(parameter.children, parameterTypeNamespace, subParameters, xpath, definitions, parameterTypeNSContext, globalNameSpaceContext);
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

	public static final String SOAPENV = "_soapenv_";
	public static final String XSD = "_xsd_";
	public static final String WSDL = "_wsdl_";

	private Map<String, String> nameSpaceURIS;

	public SoapDustNameSpaceContext() {
		nameSpaceURIS = new HashMap<String, String>();
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