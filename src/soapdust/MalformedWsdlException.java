package soapdust;

import org.xml.sax.SAXException;

/**
 * Thrown when a wsdl can not be parsed.
 */
public class MalformedWsdlException extends Exception {

	public MalformedWsdlException(String message, SAXException e) {
		super(message, e);
	}
}
