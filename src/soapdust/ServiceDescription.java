package soapdust;

import java.util.HashMap;
import java.util.Map;

public class ServiceDescription {

	public Map<String, WsdlOperation> operations = new HashMap<String, WsdlOperation>();
	public Map<String, WsdlElement> messages = new HashMap<String, WsdlElement>();

}
