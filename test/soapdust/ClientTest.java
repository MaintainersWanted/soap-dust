package soapdust;

import java.io.IOException;
import java.io.StringWriter;

import junit.framework.TestCase;

public class ClientTest extends TestCase {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public void testExplainWsdl() throws IOException, MalformedWsdlException {
		StringWriter result = new StringWriter();
		Client client = new Client();
		client.setWsdlUrl("file:test/soapdust/test.wsdl");

		client.explain(result);
		
		assertEquals(
				"testOperation1"                  + LINE_SEPARATOR +
				"\t" + "testOperation1"           + LINE_SEPARATOR +
				"\t\t" + "sender"                 + LINE_SEPARATOR +
				"\t\t" + "MSISDN"                 + LINE_SEPARATOR +
				"\t\t" + "IDOffre"                + LINE_SEPARATOR +
				"\t\t" + "doscli"                 + LINE_SEPARATOR +
				"\t\t\t" + "subParameter1"        + LINE_SEPARATOR +
				"\t\t\t" + "subParameter2"        + LINE_SEPARATOR +
				"\t\t\t" + "subParameter3"        + LINE_SEPARATOR +
				"\t\t\t" + "subParameter4"        + LINE_SEPARATOR +
				"\t\t\t\t" + "message"            + LINE_SEPARATOR +
				"\t\t\t\t" + "untyped"            + LINE_SEPARATOR +
				"\t" + "messageParameter2"        + LINE_SEPARATOR +
				"testOperation2"                  + LINE_SEPARATOR 
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