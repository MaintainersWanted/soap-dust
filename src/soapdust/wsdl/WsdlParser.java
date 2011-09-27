package soapdust.wsdl;

import static soapdust.wsdl.XMLUtil.SOAP_NS;
import static soapdust.wsdl.XMLUtil.WSDL_NS;
import static soapdust.wsdl.XMLUtil.attribute;
import static soapdust.wsdl.XMLUtil.attributeOrNull;
import static soapdust.wsdl.XMLUtil.children;
import static soapdust.wsdl.XMLUtil.newXmlParser;
import static soapdust.wsdl.XMLUtil.typeDescription;
import static soapdust.wsdl.XMLUtil.validateWsdl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * wsdl 1.1 parser
 */
public class WsdlParser {

	private final URL context;

	public WsdlParser(URL context) {
		this.context = context;
	}
	
	public WebServiceDescription parse() throws SAXException, IOException, ParserConfigurationException {
		return parse(new WebServiceDescription());
	}

	private WebServiceDescription parse(WebServiceDescription description) throws SAXException, IOException, ParserConfigurationException {
		InputStream inputStream = context.openStream();

		DocumentBuilder parser = newXmlParser();
		Document document = parser.parse(inputStream);
//		validateWsdl(document); we have problems with this with jdk1.5 and with androïd so...
		Node definitionNode = children(document, WSDL_NS , "definitions").get(0);
		return parse(description, definitionNode);
	}

	private WebServiceDescription parse(WebServiceDescription description, Node definitionNode) throws SAXException, IOException, ParserConfigurationException {
		
		Definition definition = description.newDefinition(attribute(definitionNode, "targetNamespace"));
		
		parseImportWsdl(description, definitionNode);
		
		parseTypes(description, definitionNode);
		
		parseMessages(description, definitionNode, definition);
		
		parseOperations(description, definitionNode, definition);

		return description;
	}

	private void parseOperations(WebServiceDescription description,
			Node definitionNode, Definition definition)
			throws SAXParseException {
		for (Node portNode: children(definitionNode, WSDL_NS , "portType")) {
			for (Node operationNode: children(portNode, WSDL_NS , "operation")) {
				String operationName = attribute(operationNode, "name");
				Operation operation = definition.newOperation(operationName);
				operation.input = operationMessage(description, definition, operationNode, "input");
				operation.output = operationMessage(description, definition, operationNode, "output");
			}
		}
		
		for (Node wsdlBindingNode: children(definitionNode, WSDL_NS , "binding")) {
			int defaultStyle = Operation.STYLE_DOCUMENT;
			for (Node soapBindingNode: children(wsdlBindingNode, SOAP_NS , "binding")) {
				defaultStyle = Operation.toStyle(attribute(soapBindingNode, "style"));
			}
			for (Node wsdlOperationNode: children(wsdlBindingNode, WSDL_NS , "operation")) {
				for (Node soapOperationNode: children(wsdlOperationNode, SOAP_NS , "operation")) {
				    
				    //FIXME we should really search for the operation in the good definition using
				    //      the binding's type instead of randomly searching using its name.
				    //      see binding type here : http://www.w3.org/TR/wsdl#_bindings
				    //      see definition ref here : http://www.w3.org/TR/wsdl#_document-n
//					Operation operation = definition.operations.get(attribute(wsdlOperationNode, "name"));
				    Operation operation = description.findOperation(attribute(wsdlOperationNode, "name"));
                    
					operation.style = Operation.toStyle(attribute(soapOperationNode, "style"), defaultStyle);
					operation.soapAction = attributeOrNull(soapOperationNode, "soapAction");
				}				
			}
		}
	}

	private Message operationMessage(WebServiceDescription description,
			Definition definition, Node operationNode, String nature) 
	throws SAXParseException {
		List<Node> nodes = children(operationNode, WSDL_NS , nature);
		if (nodes.size() == 1) {
			Node node = nodes.get(0); //there is an output node
			String[] messageDescription = 
				typeDescription(attribute(node, "message"), definition.nameSpace, node);
			return description.getDefinition(messageDescription[0]).getMessage(messageDescription[1]);
		}
		return null;
	}

	private void parseMessages(WebServiceDescription description,
			Node definitionNode, Definition definition) throws SAXParseException {
		for (Node messageNode : children(definitionNode, WSDL_NS , "message")) {
			Message message = definition.newMessage(attribute(messageNode, "name"));
			for (Node partNode : children(messageNode, WSDL_NS , "part")) {
				String partName = attribute(partNode, "name");
				String partType = attribute(partNode, "element", "type");
				String[] typeDescription = 
					typeDescription(partType, definition.nameSpace, partNode);
				message.newPart(partName, 
						description.xsd.findType(typeDescription[0], typeDescription[1]));
			}
		}
	}

	private void parseTypes(WebServiceDescription description,
			Node definitionNode) throws FileNotFoundException,
			ParserConfigurationException, SAXException, IOException {
		for (Node typeNode: children(definitionNode, WSDL_NS , "types")) {
			new XSDParser(this.context).parse(typeNode, description.xsd);
		}
		description.xsd.purgePendingSchemas();
	}

	private void parseImportWsdl(WebServiceDescription description, Node definitionNode) 
	throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		for (Node importNode : children(definitionNode, WSDL_NS , "import")) {
			String namespace = attribute(importNode, "namespace");
			String location = attributeOrNull(importNode, "location");
			URL url;
			if (location != null) {
				url = new URL(context, location);
			} else {
				url = new URL(context, namespace);
			}
			new WsdlParser(url).parse(description);
		}
	}
}
