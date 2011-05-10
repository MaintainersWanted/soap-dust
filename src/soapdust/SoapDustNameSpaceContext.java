package soapdust;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

class SoapDustNameSpaceContext implements NamespaceContext {

	public static final String SOAP = "_soap_";
	public static final String SOAPENV = "_soapenv_";
	public static final String XSD = "_xsd_";
	public static final String WSDL = "_wsdl_";

	private Map<String, String> nameSpaceURIS;

	public SoapDustNameSpaceContext() {
		nameSpaceURIS = new HashMap<String, String>();
		nameSpaceURIS.put(SOAP, "http://schemas.xmlsoap.org/wsdl/soap/");
		nameSpaceURIS.put(WSDL, "http://schemas.xmlsoap.org/wsdl/");
		nameSpaceURIS.put(XSD, "http://www.w3.org/2001/XMLSchema");
		nameSpaceURIS.put(SOAPENV, "http://schemas.xmlsoap.org/soap/envelope/");
	}

	public SoapDustNameSpaceContext(SoapDustNameSpaceContext parent) {
		this.nameSpaceURIS = new HashMap<String, String>(parent.nameSpaceURIS);
	}
	
	public String getNamespaceURI(String prefix) {
		return nameSpaceURIS.get(prefix);
	}

	public String getPrefix(String namespaceURI) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("rawtypes")
	public Iterator getPrefixes(String namespaceURI) {
		throw new UnsupportedOperationException();
	}
	
	public void addNamespace(String prefix, String uri) {
		nameSpaceURIS.put(prefix, uri);
	}
}