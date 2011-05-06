package soapdust.wsdl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

class XMLUtil {

	static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/";
	static final String SOAP_NS = "http://schemas.xmlsoap.org/wsdl/soap/";

	static List<Node> children(Node parent, String childNS, String... childLocalName) {
		List<String> childNames = Arrays.asList(childLocalName);
		ArrayList<Node> result = new ArrayList<Node>();

		NodeList children = parent.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (childNS.equals(child.getNamespaceURI())
					&& childNames.contains(child.getLocalName())) {

				result.add(child);
			}
		}
		return result;
	}

	static DocumentBuilder newXmlParser() throws ParserConfigurationException, MalformedURLException, SAXException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
		return parser;
	}
	
	static void validateWsdl(Document document) throws SAXException, FileNotFoundException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		javax.xml.validation.Schema schema = 
			schemaFactory.newSchema(new StreamSource(XMLUtil.class.getResourceAsStream("wsdl.xsd")));
		
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new DefaultHandler() {
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		validator.validate(new DOMSource(document));
	}

	static void validateXSD(Document document) throws SAXException, FileNotFoundException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		javax.xml.validation.Schema schema = 
			schemaFactory.newSchema(new StreamSource(XMLUtil.class.getResourceAsStream("xsd.xsd")));
		
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new DefaultHandler() {
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		validator.validate(new DOMSource(document));
	}

	static String attribute(Node node, String... attributes) {
		for(int i = 0; i < attributes.length; i++) {
			String attribute = attributes[i];
			Node attr = node.getAttributes().getNamedItem(attribute);
			if (attr != null) return attr.getNodeValue();
		}
		return "";
	}
	
	static String[] typeDescription(String type, String defaultNameSpace, Node node) throws SAXParseException {
		final String typeNameSpace;
		final String typeName;
		String[] split = type.split(":");
		switch (split.length) {
		case 1:
			typeNameSpace = defaultNameSpace;
			typeName = split[0];
			break;
		case 2:
			typeNameSpace = node.lookupNamespaceURI(split[0]);
			typeName = split[1];
			break;
		default:
			throw new SAXParseException("Illegal type format: " + type, null);
		}
		return new String[] {typeNameSpace, typeName};
	}
}
