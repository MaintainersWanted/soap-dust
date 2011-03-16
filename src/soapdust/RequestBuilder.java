package soapdust;

import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

class RequestBuilder {

	private final ServiceDescription serviceDescription;

	RequestBuilder(ServiceDescription serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	
	Document build(String operation, ComposedValue parameters) {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			Document document = documentBuilder.newDocument();

			Element operationElement = createOperationElement(operation, document);
			WsdlOperation wsdlOperation = serviceDescription.operations.get(operation);
			addParameters(document, operationElement, parameters, wsdlOperation.parts, wsdlOperation.namespace);
			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("Unexpected exception while preparing soap request: " + e, e);
		}
	}
	
	private Element createOperationElement(String operation, Document document) {

		Element envelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		Element header = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
		Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");

		document.appendChild(envelope);
		envelope.appendChild(header);
		envelope.appendChild(body);

		WsdlOperation wsdlOperation = serviceDescription.operations.get(operation);
		switch (wsdlOperation.getStyle()) {
		case WsdlOperation.RPC:
			Element operationElement = document.createElementNS(wsdlOperation.namespace, operation);
			body.appendChild(operationElement);
			return operationElement;
		case WsdlOperation.DOCUMENT:
		default:
			return body;
		}
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
				addParameters(document, param, (ComposedValue) childValue, paramWsdlElement == null ? parent : paramWsdlElement.children, namespace);
			} else {
				throw new IllegalArgumentException("ComposedValue can only be composed of ComposedValue or String, not: " + childValue.getClass());
			}
		}
	}
}
