package soapdust;

import org.xml.sax.SAXException;

public class MalformedWsdlException extends Exception {

	public MalformedWsdlException(String message, SAXException e) {
		super(message, e);
	}

}
