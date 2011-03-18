package soapdust;

/**
 * Thrown when the server returned invalid soap data.
 * 
 * It sometimes occurs when a basic authentication fails...
 */
public class MalformedResponseException extends Exception {

	/**
	 *	The http status code returned by the server.
	 */
	public int responseCode = 200; //default response code
	
	/**
	 * The invalid soap data sent by the server. This field may be null.
	 * 
	 * @see Client.activeTraceMode() if you want to ensure this field
	 * is NEVER null.
	 */
	public byte[] response;

	public MalformedResponseException(String message, Exception e) {
		super(message, e);
	}

	public MalformedResponseException(String message, 
			int responseCode, byte[] data) {
		super(message);
		this.responseCode = responseCode;
		this.response = data;
	}
}
