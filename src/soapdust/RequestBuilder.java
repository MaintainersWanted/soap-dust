package soapdust;

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

class RequestBuilder {

	private final WebServiceDescription serviceDescription;

	RequestBuilder(WebServiceDescription serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	
	Document build(String operationName, ComposedValue parameters) {
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
	
	private Element createOperationElement(String operationName, Document document) {

		Element envelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		Element header = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
		Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");

		document.appendChild(envelope);
		envelope.appendChild(header);
		envelope.appendChild(body);

		Operation operation = serviceDescription.findOperation(operationName);
		switch (operation.style) {
		case Operation.STYLE_RPC:
			Element operationElement = document.createElementNS(operation.definition.nameSpace, operationName);
			body.appendChild(operationElement);
			return operationElement;
		case Operation.STYLE_DOCUMENT:
		default:
			return body;
		}
	}			

	private void addParameters(Document document, Element operationElement,
			Operation operation, ComposedValue parameters) {
		Message message = operation.input;
		
		for (String childKey : parameters.getChildrenKeys()) {
			Part part;
			String partNamespace;
			switch(operation.style) {
			case Operation.STYLE_DOCUMENT:
				part = message.getPartByTypeName(childKey);
				Type type = part.type;
				partNamespace = type.namespace;
				break;
			case Operation.STYLE_RPC:
			default:
				part = message.getPart(childKey);
				partNamespace = part.namespace(); //FIXME is it possible that we do not find any part here ? If so -> NPE
				break;
			}
			Element param = document.createElementNS(partNamespace, childKey);
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
			Type type = parentType.getType(childKey);
			Element param = document.createElementNS(type.namespace, childKey);
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
}