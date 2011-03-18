package soapdust;

/**
 * Thrown when the remote server returns a soap fault.
 */
public class FaultResponseException extends Exception {
	
	/**
	 * The fault returned by the remote server.
	 */
	public ComposedValue fault;
	
	/**
	 * The http status code. Generally 500 in this case.
	 */
	public int responseCode;

	public FaultResponseException(String message, ComposedValue fault, int responseCode) {
		super(message);
		this.fault = fault;
		this.responseCode = responseCode;
	}
}
