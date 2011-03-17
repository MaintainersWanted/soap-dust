package soapdust;

public class FaultResponseException extends Exception {

	public ComposedValue fault;
	public int responseCode;

	public FaultResponseException(String message, ComposedValue fault, int responseCode) {
		super(message);
		this.fault = fault;
		this.responseCode = responseCode;
	}
}
