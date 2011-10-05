package soapdust.wsdl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class WebServiceDescription {

//	TODO Detect document-wrapped wsdl:
//	Extracted from ExamplesWSDL.html
//	  * The input message has a single part.
//    * The part is an element.
//    * The element has the same name as the operation.
//    * The element's complex type has no attributes.

	
	public XSD xsd = new XSD();
	private Map<String, Definition> definitions = new HashMap<String, Definition>();

	public Type findType(String targetNameSpace, String name) {
		return xsd.findType(targetNameSpace, name);
	}
	
	public Operation findOperation(String operationName) {
		for (Definition definition : getDefinitions()) {
			Operation operation = definition.operations.get(operationName);
			if (operation != null) return operation;
		}
		throw new UnsupportedOperationException("operation not found in service description (check Client.explain()): " + operationName);
	}
	
	public Schema getSchema(String targetNameSpace) {
		return xsd.schemas.get(targetNameSpace);
	}
	
	public Collection<Schema> getSchemas() {
		return xsd.schemas.values();
	}
	
	public Definition getDefinition(String nameSpace) {
		return definitions.get(nameSpace);
	}
	
	public Collection<Definition> getDefinitions() {
		return definitions.values();
	}
	
	public Definition newDefinition(String nameSpace) {
		Definition definition = new Definition(nameSpace);
		definitions.put(nameSpace, definition);
		return definition;
	}
}
