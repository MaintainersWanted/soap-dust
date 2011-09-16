package soapdust.wsdl;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Type implements TypeContainer {

	public final String namespace;
	public final String name;
	public boolean qualified; //FIXME by default in the spec it should be unqualified
	
	private final LinkedHashMap<String, Type> types = new LinkedHashMap<String, Type>();

	public Type(String ns, String name, boolean qualified) {
		this.namespace = ns;
		this.name = name;
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

	@Override
	public String toString() {
		return namespace + "|" + name + getTypes();
	}


}
