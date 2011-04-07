package soapdust.wsdl;

import java.util.HashMap;
import java.util.Map;

public class WebServiceDescription {

	public Map<String, Schema> schemas = new HashMap<String, Schema>();

	public Type findType(String targetNameSpace, String name) {
		Schema schema = schemas.get(targetNameSpace);
		return schema == null ? null : schema.getType(name);
	}
}
