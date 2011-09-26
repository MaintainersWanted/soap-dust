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

	public FaultResponseException(ComposedValue fault, int responseCode) {
		super(faultMessage(fault));
		this.fault = fault;
		this.responseCode = responseCode;
	}

	public FaultResponseException(ComposedValue fault) {
		this(fault, 500);
	}

	private static String faultMessage(ComposedValue fault) {
		String message = fault.getChildrenKeys().contains("faultstring") ? fault.getStringValue("faultstring") : null;
		return message;
	}
}
