package soapdust.wsdl;

import static soapdust.wsdl.XMLUtil.SOAP_NS;
import static soapdust.wsdl.XMLUtil.WSDL_NS;
import static soapdust.wsdl.XMLUtil.attribute;
import static soapdust.wsdl.XMLUtil.attributeOrNull;
import static soapdust.wsdl.XMLUtil.children;
import static soapdust.wsdl.XMLUtil.newXmlParser;
import static soapdust.wsdl.XMLUtil.typeDescription;
import static soapdust.wsdl.XMLUtil.validateWsdl;

import java.io.IOException;
import java.io.InputStream;
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
		return parse(context.openStream());
	}
	
	public WebServiceDescription parse(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {
		
		DocumentBuilder parser = newXmlParser();

		Document document = parser.parse(inputStream);
		
		validateWsdl(document);
		
		WebServiceDescription description = new WebServiceDescription();

		Node definitions = children(document, WSDL_NS , "definitions").get(0);
		String targetNameSpace = attribute(definitions, "targetNamespace");
		
		Definition definition = description.newDefinition(targetNameSpace);
		
		for (Node typeNode: children(definitions, WSDL_NS , "types")) {
			new XSDParser(this.context).parse(typeNode, description.xsd);
		}
		description.xsd.purgePendingSchemas();
		
		for (Node messageNode : children(definitions, WSDL_NS , "message")) {
			parseMessage(description.xsd, definition, messageNode);
		}
		
		for (Node portNode: children(definitions, WSDL_NS , "portType")) {
			for (Node operationNode: children(portNode, WSDL_NS , "operation")) {
				String operationName = attribute(operationNode, "name");
				Operation operation = definition.newOperation(operationName);
				List<Node> inputNodes = children(operationNode, WSDL_NS , "input");
				if (inputNodes.size() == 1) {
					Node inputNode = inputNodes.get(0); //there is an input node
					String[] messageDescription = 
						typeDescription(attribute(inputNode, "message"), targetNameSpace, inputNode);
					operation.input = 
						description.getDefinition(messageDescription[0]).getMessage(messageDescription[1]);
				}
			}
		}
		
		for (Node wsdlBindingNode: children(definitions, WSDL_NS , "binding")) {
			int defaultStyle = Operation.STYLE_DOCUMENT;
			for (Node soapBindingNode: children(wsdlBindingNode, SOAP_NS , "binding")) {
				defaultStyle = Operation.toStyle(attribute(soapBindingNode, "style"));
			}
			for (Node wsdlOperationNode: children(wsdlBindingNode, WSDL_NS , "operation")) {
				for (Node soapOperationNode: children(wsdlOperationNode, SOAP_NS , "operation")) {
					Operation operation = definition.operations.get(attribute(wsdlOperationNode, "name"));
					operation.style = Operation.toStyle(attribute(soapOperationNode, "style"), defaultStyle);
					operation.soapAction = attributeOrNull(soapOperationNode, "soapAction");
				}				
			}
		}

		return description;
	}

	private void parseMessage(XSD xsd, Definition definition, Node messageNode) throws SAXParseException {
		
		Message message = definition.newMessage(attribute(messageNode, "name"));
		
		for (Node partNode : children(messageNode, WSDL_NS , "part")) {
			newPart(xsd, definition, message, partNode);
		}
	}

	private void newPart(XSD xsd, Definition definition, Message message,
			Node partNode) throws SAXParseException {

		String[] typeDescription = typeDescription(attribute(partNode, "element", "type"), 
				definition.nameSpace, partNode);
		Type partType = xsd.findType(typeDescription[0], typeDescription[1]);
		message.newPart(attribute(partNode, "name"), partType);
	}
}
