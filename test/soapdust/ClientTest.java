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
				"\t" + "messageParameter1Element" + LINE_SEPARATOR +
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
				"testOperation2"                  + LINE_SEPARATOR 
				, result.toString());
	}
	
	public void testExplainDocumentLiteralWsdl() throws IOException, MalformedWsdlException {
		StringWriter result = new StringWriter();
		Client client = new Client();
		client.setWsdlUrl("file:test/soapdust/document-literal.wsdl");

		client.explain(result);
		
		assertEquals(
				"myMethod"         + LINE_SEPARATOR +
				"\t" + "xElement"  + LINE_SEPARATOR +
				"\t" + "yElement"  + LINE_SEPARATOR,
				result.toString());
	}

	public void testExplainDocumentWrappedWsdl() throws IOException, MalformedWsdlException {
		StringWriter result = new StringWriter();
		Client client = new Client();
		client.setWsdlUrl("file:test/soapdust/wsdl/document-wrapped.wsdl");

		client.explain(result);
		
		assertEquals(
				"myMethod"  + LINE_SEPARATOR +
				"\t" + "x"  + LINE_SEPARATOR +
				"\t" + "y"  + LINE_SEPARATOR,
				result.toString());
	}
	
	public void testExplainNotDocumentWrappedWsdl() throws IOException, MalformedWsdlException {
		StringWriter result = new StringWriter();
		Client client = new Client();
		client.setWsdlUrl("file:test/soapdust/wsdl/document-style-with-single-parameter-not-named-after-operation-name.wsdl");

		client.explain(result);
		
		assertEquals(
				"myMethod"              + LINE_SEPARATOR +
				"\t" + "myMethodParams" + LINE_SEPARATOR +
				"\t" + "\t" + "x"       + LINE_SEPARATOR +
				"\t" + "\t" + "y"       + LINE_SEPARATOR,
				result.toString());
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