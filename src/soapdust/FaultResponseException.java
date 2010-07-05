package soapdust;

public class FaultResponseException extends Exception {

	public ComposedValue fault;
	public int responseCode;
	public byte[] response;

	public FaultResponseException(String message, ComposedValue fault, byte[] data, int responseCode) {
		super(message);
		this.fault = fault;
		this.response = data;
		this.responseCode = responseCode;
	}
}
