package soapdust.wsdl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Message {

	private Map<String, Part> parts = new LinkedHashMap<String, Part>();
	private Map<String, Part> partsByTypeName = new LinkedHashMap<String, Part>();
	private final Definition definition;

	public Message(Definition definition) {
		this.definition = definition;
	}

	public Part newPart(String partName, Type partType) {
		Part part = new Part(this, partName, partType);
		parts.put(partName, part);
		if(partType != null) {
			//FIXME is this normal ?
			//      it occurs for root xsd types but...
			partsByTypeName.put(partType.name, part);
		}
		return part;
	}

	public Part getPart(String partName) {
		return parts.get(partName);
	}

	public Part getPartByTypeName(String typeName) {
		return partsByTypeName.get(typeName);
	}
	
	public Collection<Part> getParts() {
		return parts.values();
	}

    public Map<String, Part> getPartsMap() {
        return Collections.unmodifiableMap(parts);
    }

    public Set<String> getPartsTypes() {
        return partsByTypeName.keySet();
    }

	public String namespace() {
		return definition.nameSpace;
	}

	public int getPartNumber() {
		return parts.size();
	}
}
