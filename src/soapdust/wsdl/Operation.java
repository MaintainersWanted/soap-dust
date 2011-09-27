package soapdust.wsdl;

public class Operation {

	public static final int STYLE_DOCUMENT = 1;
	public static final int STYLE_RPC = 2;
	
	public int style;
	public String soapAction;
	public Message input;
	public Message output;
	public String name;
	public final Definition definition;
	
	public Operation(Definition parent, String name) {
		this.definition = parent;
		this.name = name;
		this.style = STYLE_DOCUMENT; //default style
	}

	public static int toStyle(String style) {
		if ("rpc".equals(style)) return STYLE_RPC;
		return STYLE_DOCUMENT; //default style
	}

	public static int toStyle(String style, int defaultStyle) {
		if ("document".equals(style)) return STYLE_DOCUMENT;
		if ("rpc".equals(style)) return STYLE_RPC;
		return defaultStyle;
	}
}
