package soapdust.wsdl;

import java.util.HashMap;
import java.util.Map;

public class Schema {

	public final String targetNameSpace;

	private final Map<String, Type> types = new HashMap<String, Type>();
	private final WebServiceDescription description;

	public Schema(WebServiceDescription description, String targetNameSpace) {
		this.description = description;
		this.targetNameSpace = targetNameSpace;
	}
	
	public void addType(String name, Type type) {
		types.put(name, type);
	}
	
	public Type getType(String name) {
		return types.get(name);
	}
	
	public Type findType(String targetNameSpace, String name) {
		if (this.targetNameSpace.equals(targetNameSpace)) {
			return getType(name);
		} else {
			return this.description.findType(targetNameSpace, name);
		}
	}
} 
