package soapdust.wsdl;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Type implements TypeContainer {

	public final String nameSpace;
	public final String name;
	
	private final LinkedHashMap<String, Type> types = new LinkedHashMap<String, Type>();

	public Type(String ns, String name) {
		this.nameSpace = ns;
		this.name = name;
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

	@Override
	public String toString() {
		return nameSpace + "|" + name + getTypes();
	}


}
