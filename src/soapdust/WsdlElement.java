package soapdust;

import java.util.HashMap;
import java.util.Map;

public class WsdlElement {

	public String namespace;
	public Map<String, WsdlElement> children = new HashMap<String, WsdlElement>();
	
	public WsdlElement(String namespace) {
		this.namespace = namespace;
	}
}
