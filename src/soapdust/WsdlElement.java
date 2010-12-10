package soapdust;

import java.util.LinkedHashMap;
import java.util.Map;

public class WsdlElement {

	public String namespace;
	public Map<String, WsdlElement> children = new LinkedHashMap<String, WsdlElement>();
	
	public WsdlElement(String namespace) {
		this.namespace = namespace;
	}
}
