package soapdust.wsdl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Schema implements TypeContainer {

	public final String targetNameSpace;
	public final boolean qualified;

	private final Map<String, Type> types = new HashMap<String, Type>();
	private final XSD xsd;


	public Schema(XSD xsd, String targetNameSpace, boolean qualified) {
		this.xsd = xsd;
		this.targetNameSpace = targetNameSpace;
        this.qualified = qualified;
	}

	public Collection<Type> getTypes() {
		return types.values();
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
			return this.xsd.findType(targetNameSpace, name);
		}
	}
	
	@Override
	public String toString() {
		return "[" + targetNameSpace + ":" + xsd.schemas + "]";
	}
} 
