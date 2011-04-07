package soapdust.wsdl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

public class WsdlParser {

	public WebServiceDescription parse(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {
		
		DocumentBuilder parser = newXmlParser();

		Document document = parser.parse(inputStream);
		
		validate(document);
		
		WebServiceDescription description = new WebServiceDescription();
		
		NodeList typesDefinitions = document.getElementsByTagNameNS("http://schemas.xmlsoap.org/wsdl/" , "types");
		for(int i = 0; i < typesDefinitions.getLength(); i++) {
			List<Node> schemas = children(typesDefinitions.item(i), "http://www.w3.org/2001/XMLSchema", "schema");
			for (Node schemaNode : schemas) {
				parseSchema(schemaNode, description);
			}
		}
		return description;
	}

	private void parseSchema(Node schemaNode, WebServiceDescription description) throws SAXParseException {
		Schema schema = new Schema(description, attribute(schemaNode, "targetNamespace"));
		description.schemas.put(schema.targetNameSpace, schema);
		
		for (Node element : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "element")) {
			Type type = addType(schema, attribute(element, "name"), newType(schema, element));
			if(attribute(element, "type").equals("")) {
				parseComplexTypes(schema, element, type);
			}
		}
		for (Node typeNode : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "complexType")) {
			Type type = addType(schema, attribute(typeNode, "name"), newType(schema, typeNode));
			parseSequences(schema, typeNode, type);
		}
		for (Node typeNode : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "simpleType")) {
			addType(schema, attribute(typeNode, "name"), newType(schema, typeNode));
		}
	}

	private void parseComplexTypes(Schema schema, Node parent, Type type) throws SAXParseException {
		for (Node typeNode : children(parent, "http://www.w3.org/2001/XMLSchema", "complexType")) {
			parseSequences(schema, typeNode, type);
		}
	}

	private void parseSequences(Schema schema, Node typeNode, Type type) throws SAXParseException {
		List<Node> sequences = children(typeNode, "http://www.w3.org/2001/XMLSchema", "sequence");
		for (Node sequence : sequences) {
			for (Node element : children(sequence, "http://www.w3.org/2001/XMLSchema", "element")) {
				type.addElement(attribute(element, "name"), newType(schema, element));
			}
		}
	}

	private Type newType(Schema schema, Node typeNode) throws SAXParseException {
		if(attribute(typeNode, "type").equals("")) {
			return new Type(schema.targetNameSpace, attribute(typeNode, "name"));
		} else {
			return newDelegateType(schema, typeNode);
		}
	}

	private Type newDelegateType(Schema schema, Node typeNode)
			throws SAXParseException {
		String name = attribute(typeNode, "name");
		DelegateType type;
		{
			String typeAttr = attribute(typeNode, "type");
			String[] split = typeAttr.split(":");
			switch (split.length) {
			case 1:
				type = new DelegateType(schema, schema.targetNameSpace, name, 
						schema.targetNameSpace, split[0]);
				break;
			case 2:
				type = new DelegateType(schema, schema.targetNameSpace, name, 
						typeNode.lookupNamespaceURI(split[0]), split[1]);
				break;
			default:
				throw new SAXParseException("Illegal type format: " + typeAttr, null);
			}
		}
		return type;
	}

	private Type addType(Schema schema, String name, Type type) {
		schema.addType(name, type);
		return type;
	}

	private String attribute(Node node, String attribute) {
		Node attr = node.getAttributes().getNamedItem(attribute);
		return attr == null ? "" : attr.getNodeValue();
	}
	
	private List<Node> children(Node parent, String childNS, String... childLocalName) {
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

	private DocumentBuilder newXmlParser() throws ParserConfigurationException, MalformedURLException, SAXException {

		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder parser = documentBuilderFactory.newDocumentBuilder();
		return parser;
	}
	
	private void validate(Document document) throws SAXException,
	FileNotFoundException, IOException {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		javax.xml.validation.Schema schema = 
			schemaFactory.newSchema(new StreamSource(this.getClass().getResourceAsStream("wsdl.xsd")));
		
		Validator validator = schema.newValidator();
		validator.setErrorHandler(new DefaultHandler() {
			public void error(SAXParseException e) throws SAXException {
				throw e;
			}
		});
		validator.validate(new DOMSource(document));
	}
}
