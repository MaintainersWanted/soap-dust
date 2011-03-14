package soapdust;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

public class ClientTest extends TestCase {

	public void testExplainWsdl() throws IOException, MalformedWsdlException {
		StringWriter result = new StringWriter();
		Client client = new Client();
		client.setWsdlUrl("file:test/soapdust/test.wsdl");

		client.explain(result);
		
		String lineSeparator = System.getProperty("line.separator");
		
		assertEquals(
				"testOperation1"                  + lineSeparator +
				"\t" + "messageParameter1Element" + lineSeparator +
				"\t\t" + "sender"                 + lineSeparator +
				"\t\t" + "MSISDN"                 + lineSeparator +
				"\t\t" + "IDOffre"                + lineSeparator +
				"\t\t" + "doscli"                 + lineSeparator +
				"\t\t\t" + "subParameter1"        + lineSeparator +
				"\t\t\t" + "subParameter2"        + lineSeparator +
				"\t\t\t" + "subParameter3"        + lineSeparator +
				"\t\t\t" + "subParameter4"        + lineSeparator +
				"\t\t\t\t" + "message"            + lineSeparator +
				"\t\t\t\t" + "untyped"            + lineSeparator +
				"\t" + "string"                   + lineSeparator +
				"testOperation2"                  + lineSeparator 
				, result.toString());
	}
	
	public void testSetWsdlUsesCache() throws IOException, MalformedWsdlException {
		String longToParseWsdl = "file:test/soapdust/jira.wsdl";
		new Client().setWsdlUrl(longToParseWsdl);
		
		long start = System.currentTimeMillis();
		new Client().setWsdlUrl(longToParseWsdl);
		long duration = System.currentTimeMillis() - start;
		
		assertTrue(duration < 100);
	}
}