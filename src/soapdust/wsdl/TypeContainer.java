package soapdust.wsdl;

import java.util.Collection;

public interface TypeContainer {

	public Collection<Type> getTypes();

	public void addType(String name, Type type);
	
	public Type getType(String name);
	
}
