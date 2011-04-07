package soapdust.wsdl;

import java.util.Collection;
import java.util.LinkedHashMap;

public class Type {

	public final String nameSpace;
	public final String name;
	
	private final LinkedHashMap<String, Type> elements = new LinkedHashMap<String, Type>();

	public Type(String ns, String name) {
		this.nameSpace = ns;
		this.name = name;
	}
	
	public Collection<Type> getElements() {
		return elements.values();
	}

	public void addElement(String name, Type type) {
		elements.put(name, type);
	}

	public Type getElement(String name) {
		return elements.get(name);
	}

	@Override
	public String toString() {
		return nameSpace + "|" + name;
	}


}
