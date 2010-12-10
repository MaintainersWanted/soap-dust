package soapdust;

import java.util.LinkedHashMap;
import java.util.Map;

public class WsdlOperation {
	public static final int RPC = 0;
	public static final int DOCUMENT = 1;
	public String soapAction;
	public Map<String, WsdlElement> parts = new LinkedHashMap<String, WsdlElement>();
	
	private int style;

	public WsdlOperation(String soapAction) {
		this.soapAction = soapAction;
	}

	public void setStyle(String style) {
		if ("document".equals(style)) {
			this.style = DOCUMENT;
		} else if ("rpc".equals(style)) {
			this.style = RPC;
		}
	}

	public int getStyle() {
		return this.style;
	}
}
