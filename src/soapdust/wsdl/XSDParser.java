package soapdust.wsdl;

import static soapdust.wsdl.XMLUtil.children;
import static soapdust.wsdl.XMLUtil.newXmlParser;
import static soapdust.wsdl.XMLUtil.attribute;
import static soapdust.wsdl.XMLUtil.typeDescription;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XSDParser {
	
	private final URL context;

	public XSDParser(URL context) {
		this.context = context;
	}
	
	public void parse(XSD xsd) throws ParserConfigurationException, SAXException, IOException {		
		DocumentBuilder parser = newXmlParser();

		Document document = parser.parse(context.openStream());
		
//		validateXSD(document); //FIXME do not know how to make this work ;(

		parse(document, xsd);
	}

	public void parse(Node document, XSD xsd)
			throws FileNotFoundException, ParserConfigurationException,
			SAXException, IOException {

		for (Node schemaNode : children(document, "http://www.w3.org/2001/XMLSchema", "schema")) {
			parseSchema(xsd, schemaNode);
		}
	}

	private void parseSchema(XSD xsd, Node schemaNode)
			throws ParserConfigurationException, SAXException, IOException,
			FileNotFoundException, SAXParseException {
		
		Schema schema = xsd.newSchema(attribute(schemaNode, "targetNamespace"), 
		        attribute(schemaNode, "elementFormDefault").equals("qualified") ? true : false);
		
		for (Node importNode : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "import")) {
			String nameSpace = attribute(importNode, "namespace");
			String location = attribute(importNode, "schemaLocation");
			xsd.importSchema(context, nameSpace, location);
		}
		
		parseElements(schema, schema, schemaNode);
		
		for (Node complexTypeNode : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "complexType")) {
			//global type ?
			Type type = addType(schema, schema, attribute(complexTypeNode, "name"), newType(schema, complexTypeNode));
			parseSequences(schema, type, complexTypeNode);
		}
		for (Node simpleTypeNode : children(schemaNode, "http://www.w3.org/2001/XMLSchema", "simpleType")) {
			addType(schema, schema, attribute(simpleTypeNode, "name"), newType(schema, simpleTypeNode));
		}
	}

	private void parseComplexTypes(Schema schema, TypeContainer parent, Node node) throws SAXParseException {
		for (Node typeNode : children(node, "http://www.w3.org/2001/XMLSchema", "complexType")) {
			parseSequences(schema, parent, typeNode);
		}
	}

	private void parseSequences(Schema schema, TypeContainer parent, Node node) throws SAXParseException {
		List<Node> sequences = children(node, "http://www.w3.org/2001/XMLSchema", "sequence");
		for (Node sequence : sequences) {
			parseElements(schema, parent, sequence);
		}
	}

	private void parseElements(Schema schema, TypeContainer typeContainer, Node node) throws SAXParseException {
		for (Node element : children(node, "http://www.w3.org/2001/XMLSchema", "element")) {
			parseElement(schema, typeContainer, element);
		}
	}

	private void parseElement(Schema schema, TypeContainer parentType, Node element) throws SAXParseException {
		Type type = addType(schema, parentType, attribute(element, "name"), newType(schema, element));
		if(attribute(element, "type").equals("")) {
			parseComplexTypes(schema, type, element);
		}
	}
	
	private Type newType(Schema schema, Node typeNode) throws SAXParseException {
		if(attribute(typeNode, "type").equals("")) {
			return new Type(schema.targetNameSpace, attribute(typeNode, "name"), schema.qualified);
		} else {
			return newDelegateType(schema, typeNode);
		}
	}

	private Type newDelegateType(Schema schema, Node typeNode) throws SAXParseException {
		String[] typeDescription = typeDescription(attribute(typeNode, "type"), schema.targetNameSpace, typeNode);
		return new DelegateType(schema, schema.targetNameSpace, attribute(typeNode, "name"), 
				typeDescription[0], typeDescription[1]);
	}

	private Type addType(Schema schema, TypeContainer parent, String name, Type type)
	throws SAXParseException {
		parent.addType(name, type);
		return type;
	}
}
