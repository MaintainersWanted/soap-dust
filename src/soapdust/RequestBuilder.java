package soapdust;

import java.util.Collection;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import soapdust.wsdl.Operation;
import soapdust.wsdl.Part;
import soapdust.wsdl.WebServiceDescription;

class RequestBuilder {

	private final ServiceDescription serviceDescription;
	private final WebServiceDescription serviceDescription2;

	RequestBuilder(ServiceDescription serviceDescription, WebServiceDescription serviceDescription2) {
		this.serviceDescription = serviceDescription;
		this.serviceDescription2 = serviceDescription2;
	}
	
	Document build(String operationName, ComposedValue parameters) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			Element operationElement = createOperationElement(operationName, document);
			WsdlOperation wsdlOperation = serviceDescription.operations.get(operationName);
			Operation operation = serviceDescription2.findOperation(operationName);
			boolean toRemove = true;
			if (toRemove)
				addParameters(document, operationElement, parameters, wsdlOperation.parts, operation.definition.nameSpace);
			else {
				Map<String, Part> parts = operation.input.getPartsMap();
				addParameters2(document, operationElement, parameters, parts, operation.definition.nameSpace);
			}
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

		Operation operation = serviceDescription2.findOperation(operationName);
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

	private void addParameters2(Document document, Element operationElement,
			ComposedValue parameters, Map<String, Part> parts, String nameSpace) {
//		for (String childKey : parameters.getChildrenKeys()) {
//			//TODO throw exception if parameters do not match with wsdl ?
//
//			Part part = parts.get(childKey);
//			String namespace = part != null ? part.namespace : defaultNamespace;
//
//			Element param = document.createElementNS(namespace, childKey);
//			operationElement.appendChild(param);
//
//			Object childValue = parameters.getValue(childKey);
//			if (childValue instanceof String) {
//				Text value = document.createTextNode((String) childValue);
//				param.appendChild(value);
//			} else if (childValue instanceof ComposedValue) {
//				ComposedValue child = (ComposedValue) childValue;
//				if (child.type != null) {
//					Attr attr = document.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "type");
//					attr.setNodeValue(child.type);
//					param.setAttributeNode(attr);
//				}
//                addParameters(document, param, child, part == null ? parent : part.children, namespace);
//			} else {
//				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
//			}
//		}
	}

	private void addParameters(Document document, Element operationElement, ComposedValue parameters, Map<String, WsdlElement> parent, String defaultNamespace) {

		for (String childKey : parameters.getChildrenKeys()) {
			//TODO throw exception if parameters do not match with wsdl ?

			WsdlElement paramWsdlElement = parent.get(childKey);
			String namespace = paramWsdlElement != null ? paramWsdlElement.namespace : defaultNamespace;

			Element param = document.createElementNS(namespace, childKey);
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
                addParameters(document, param, child, paramWsdlElement == null ? parent : paramWsdlElement.children, namespace);
			} else {
				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
			}
		}
	}
}
