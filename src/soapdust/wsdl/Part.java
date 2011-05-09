package soapdust.wsdl;

public class Part {

	public final Type type;
	public final String name;
	private final Message message;

	public Part(Message message, String name, Type type) {
		this.message = message;
		this.name = name;
		this.type = type;
	}
	
	public String namespace() {
		return message.namespace();
	}
}
