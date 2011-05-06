package soapdust.wsdl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Message {

	private Map<String, Part> parts = new LinkedHashMap<String, Part>();

	public void addPart(String attribute, Part newPart) {
		parts.put(attribute, newPart);
	}

	public Part getPart(String partName) {
		return parts.get(partName);
	}

	public Collection<Part> getParts() {
		return parts.values();
	}

	public Map<String, Part> getPartsMap() {
		return Collections.unmodifiableMap(parts);
	}
}
