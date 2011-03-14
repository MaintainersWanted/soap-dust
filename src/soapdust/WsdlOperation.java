package soapdust;

import java.util.LinkedHashMap;
import java.util.Map;

class WsdlOperation {
	static final int RPC = 0;
	static final int DOCUMENT = 1;
	
	String soapAction;
	Map<String, WsdlElement> parts = new LinkedHashMap<String, WsdlElement>();
	String namespace;
	
	private int style;

	WsdlOperation(String soapAction, String namespace) {
		this.soapAction = soapAction;
		this.namespace = namespace;
	}

	void setStyle(String style) {
		if ("document".equals(style)) {
			this.style = DOCUMENT;
		} else if ("rpc".equals(style)) {
			this.style = RPC;
		}
	}

	int getStyle() {
		return this.style;
	}
}
