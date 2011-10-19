package soapdust;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import soapdust.wsdl.Message;
import soapdust.wsdl.Operation;
import soapdust.wsdl.Part;
import soapdust.wsdl.Type;
import soapdust.wsdl.WebServiceDescription;

public class SoapMessageBuilder {

	private final WebServiceDescription serviceDescription;

	public SoapMessageBuilder(WebServiceDescription serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	
	public Document buildRequest(String operationName, ComposedValue parameters) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			Element operationElement = createOperationElement(operationName, document);
			Operation operation = serviceDescription.findOperation(operationName);
			addParameters(document, operationElement, operation, parameters);
			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unexpected exception while preparing soap request: " + e, e);
		}
	}
	
	public Document buildResponse(String operationName, ComposedValue parameters) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			Element operationElement = createSoapBody(document);
			Operation operation = serviceDescription.findOperation(operationName);
			addParameters(document, operationElement, operation, parameters, operation.output);
			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unexpected exception while preparing soap request: " + e, e);
		}
	}
	
	private Element createOperationElement(String operationName, Document document) {

		Element body = createSoapBody(document);

		Operation operation = serviceDescription.findOperation(operationName);
		switch (operation.style) {
		case Operation.STYLE_RPC:
			Element operationElement = createElement(document, operation.definition.nameSpace, operationName);
			body.appendChild(operationElement);
			return operationElement;
		case Operation.STYLE_DOCUMENT:
		default:
			return body;
		}
	}

	private Element createSoapBody(Document document) {
		Element envelope = createElement(document, "http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		Element header = createElement(document, "http://schemas.xmlsoap.org/soap/envelope/", "Header");
		Element body = createElement(document, "http://schemas.xmlsoap.org/soap/envelope/", "Body");

		document.appendChild(envelope);
		envelope.appendChild(header);
		envelope.appendChild(body);
		return body;
	}

	private void addParameters(Document document, Element operationElement,
			Operation operation, ComposedValue parameters) {
		
		if(operation.isDocumentWrapped()) {
			parameters = new ComposedValue().put(operation.name, parameters);
		}
		
		Message message = operation.input;
		
		addParameters(document, operationElement, operation, parameters, message);
	}

	private void addParameters(Document document, Element operationElement,
			Operation operation, ComposedValue parameters, Message message) {
		for (String childKey : parameters.getChildrenKeys()) {
			Part part;
			String partNamespace;
			switch(operation.style) {
			case Operation.STYLE_DOCUMENT:
				part = message.getPartByTypeName(childKey);
				if (part == null) throw new IllegalArgumentException("unkown message part of type " + childKey + ". Know part types are: " + message.getPartsTypes());
				Type type = part.type;
				partNamespace = type.namespace;
				break;
			case Operation.STYLE_RPC:
			default:
				part = message.getPart(childKey);
                if (part == null) throw new IllegalArgumentException("unkown message part " + childKey + ". Know parts: " + message.getPartsMap().keySet());
				partNamespace = part.namespace();
				break;
			}
			Element param = createElement(document, partNamespace, childKey);
            operationElement.appendChild(param);
			
			Object childValue = parameters.getValue(childKey);
			if (childValue instanceof String) {
				Text value = document.createTextNode((String) childValue);
				param.appendChild(value);
			} else if (childValue instanceof ComposedValue) {
				ComposedValue child = (ComposedValue) childValue;
				if (child.type != null) {
					Attr attr = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
					attr.setNodeValue(child.type);
					param.setAttributeNode(attr);
				}
				addParameters(document, param, part.type, child);
			} else {
				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
			}
		}
	}

	private void addParameters(Document document, Element parent, Type parentType,
			ComposedValue parameters) {
		for (String childKey : parameters.getChildrenKeys()) {
		    final String typeNamespace;
		    Type type = null;
		    if (parentType == null) {
		        //Try to tolerate unsupported xsd :(
		        typeNamespace = parent.getNamespaceURI();
		    } else {
		        type = parentType.getType(childKey);
		        if (type == null) {
		            //Try to tolerate unsupported xsd :(
		            typeNamespace = parentType.namespace;
		        } else {
		            typeNamespace = type.qualified ? type.namespace : ""; 
		        }
		    }
		    Element param = createElement(document, typeNamespace, childKey); 
            parent.appendChild(param);
			
			Object childValue = parameters.getValue(childKey);
			if (childValue instanceof String) {
				Text value = document.createTextNode((String) childValue);
				param.appendChild(value);
			} else if (childValue instanceof ComposedValue) {
				ComposedValue child = (ComposedValue) childValue;
				if (child.type != null) {
					Attr attr = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
					attr.setNodeValue(child.type);
					param.setAttributeNode(attr);
				}
				addParameters(document, param, type, child);
			} else {
				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
			}
		}
		
	}

	private Map<String, String> nsMap = new HashMap<String, String>();
	{
		nsMap.put("", ""); //no namespace -> no prefix
	}
	private int nsIndex = 0;
	private Element createElement(Document document, String nsUri, String tagName) {
		String nsPrefix = nsMap.get(nsUri);
		if (nsPrefix == null) {
			nsPrefix = "sdns" + (nsIndex++) + ":";
			nsMap.put(nsUri, nsPrefix);
		}
		return document.createElementNS(nsUri, nsPrefix + tagName);
//		return document.createElementNS(nsUri, tagName);
	}			
}