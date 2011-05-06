package soapdust.wsdl;

import java.util.HashMap;
import java.util.Map;

public class Definition {

	public final String nameSpace;

	private Map<String, Message> messages = new HashMap<String, Message>();

	public Map<String, Operation> operations = new HashMap<String, Operation>();

	public Definition(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public Message getMessage(String name) {
		return messages.get(name);
	}
	
	public Message newMessage(String name) {
		Message message = new Message();
		messages.put(name, message);
		return message;
	}
	
	public Operation newOperation(String name) {
		Operation operation = new Operation(this, name);
		operations.put(name, operation);
		return operation;
	}
}
