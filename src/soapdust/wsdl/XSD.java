package soapdust.wsdl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class XSD {
	public Map<String, Schema> schemas = new HashMap<String, Schema>();

	public Type findType(String targetNameSpace, String name) {
		Schema schema = schemas.get(targetNameSpace);
		return schema == null ? null : schema.getType(name);
	}
	
	public Schema getSchema(String targetNameSpace) {
		return schemas.get(targetNameSpace);
	}
	
	public Schema newSchema(String targetNameSpace, boolean qualified) {
		Schema schema = new Schema(this, targetNameSpace, qualified);
		schemas.put(targetNameSpace, schema);
		return schema;
	}
	
	public Collection<Schema> getSchemas() {
		return schemas.values();
	}
	
	private Collection<PendingSchema> pendingSchemas = new ArrayList<PendingSchema>();
	
	//schema importation is post-poned till the end of the wsdl parsing
	//It ensures that we know every schema defined inside the wsdl and
	//avoids trying to fetch something that is defined later in the file.
	//Call purgePendingSchemas to actually fetch the needed xsd files.
	void importSchema(URL context, String nameSpace, String location) {
		pendingSchemas.add(new PendingSchema(context, nameSpace, location));
	}

	void purgePendingSchemas() throws ParserConfigurationException, SAXException, IOException {
		//FIXME this is crappy code :(
		Collection<PendingSchema> oldPendingSchemas = pendingSchemas;
		for (PendingSchema pendingType : oldPendingSchemas) {
			if (schemas.get(pendingType.nameSpace) == null) {
				pendingSchemas = new ArrayList<PendingSchema>();
				final URL url;
				if (pendingType.location.equals("")) {
					url = new URL(pendingType.context, pendingType.nameSpace + ".xsd");
				} else {
					url = new URL(pendingType.context, pendingType.location);
				}
				//FIXME define the definite list of urls we do not try to fetch
				if (! "schemas.xmlsoap.org".equals(url.getHost())) {
					new XSDParser(url).parse(this);
				}
				purgePendingSchemas();
			}
		}
	}
}
class PendingSchema {

	final URL context;
	final String nameSpace;
	final String location;

	public PendingSchema(URL context, String nameSpace, String location) {
		this.context = context;
		this.nameSpace = nameSpace;
		this.location = location;
	}
}
