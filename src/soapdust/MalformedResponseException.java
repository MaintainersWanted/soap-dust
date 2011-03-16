package soapdust;

public class MalformedResponseException extends Exception {

	public int responseCode = 200; //default response code
	public byte[] response;

	public MalformedResponseException(String message, Exception e) {
		super(message, e);
	}

	public MalformedResponseException(String errorMessage, int responseCode,
			byte[] data) {
		super(errorMessage);
		this.responseCode = responseCode;
		this.response = data;
	}
}
