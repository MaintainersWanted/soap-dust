package soapdust;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class ClientResponseManagementTest extends TestCase {

	private Client client;

	@Override
	protected void setUp() throws Exception {
		client = new Client();
		client.setWsdlUrl("file:test/soapdust/test.wsdl");
	}
	
	public void testThrowsFaultExceptionInCaseOfFaultResponse() throws IOException, MalformedResponseException, MalformedWsdlException {
		client.setEndPoint("dust://status:500/test/soapdust/response-with-fault.xml");
		
		try {
			client.call("test");
			fail("FaultResponseException expected");
			
		} catch (FaultResponseException faultException) {
			assertEquals("soapenv:Server.userException", 
					faultException.fault.getStringValue("faultcode"));
			assertEquals("com.atlassian.jira.rpc.exception.RemoteAuthenticationException: Invalid username or password.", 
					faultException.fault.getStringValue("faultstring"));
			assertEquals(500, faultException.responseCode);
			assertTrue(Arrays.equals(readFile("test/soapdust/response-with-fault.xml"), 
					faultException.response));
		}
	}

	public void testParseBodyWithHref() throws IOException, SAXException, ParserConfigurationException, FaultResponseException, MalformedResponseException {
		client.setEndPoint("dust://test/soapdust/response-with-href.xml");
		
		ComposedValue result = client.call("test");

		ComposedValue return0 = result
		.getComposedValue("getIssuesFromFilterResponse")
		.getComposedValue("getIssuesFromFilterReturn").getComposedValue("getIssuesFromFilterReturn");
		ComposedValue return1 = result
		.getComposedValue("getIssuesFromFilterResponse")
		.getComposedValue("getIssuesFromFilterReturn").getComposedValue("getIssuesFromFilterReturn1");
		
		assertEquals("This is test 1", return0.getStringValue("summary"));
		assertEquals("This is test 2", return1.getStringValue("summary"));
		assertSame(return0.getComposedValue("affectsVersions"), 
				return1.getComposedValue("affectsVersions"));
	}

	public void testEmptyNodeResultInNullStringOrComposedValueAndDoesNotFail() 
	throws IOException, SAXException, ParserConfigurationException, FaultResponseException, MalformedResponseException {

		client.setEndPoint("dust://test/soapdust/response-with-empty-nodes.xml");
		
		ComposedValue result = client.call("test");

		ComposedValue version = result
		.getComposedValue("getIssuesFromFilterResponse")
		.getComposedValue("getIssuesFromFilterReturn")
		.getComposedValue("affectsVersions");
		
		assertNull(version.getStringValue("name"));
		assertNull(version.getComposedValue("name"));
	}
	
	public void testUnhandledttpStatusThrowsMalformedResponseException() throws FaultResponseException, IOException {
		client.setEndPoint("dust://status:153/test/soapdust/response-with-href.xml");//TODO add a response.wsdl file for general purpose queries
		
		try {
			client.call("test");
			fail("MalformedResponseException expected");
		} catch (MalformedResponseException e) {
			assertEquals(153, e.responseCode);
		}
	}

	public void testUnhandledHttpStatusStoresReceivedDataInException() throws IOException, FaultResponseException {
		client.setEndPoint("dust://status:153/test/soapdust/response-with-href.xml");//TODO add a response.wsdl file for general purpose queries
		try {
			client.call("test");
			fail("MalformedResponseException expected");
		} catch(MalformedResponseException e) {
			assertTrue(Arrays.equals(readFile("test/soapdust/response-with-href.xml"), e.response));
		}
	}

	
	//---

	private byte[] readFile(String file) throws IOException {
		ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[1024];

		//TODO use DataInputStream.readFully()
		for(int read = in.read(buffer, 0, buffer.length); read != -1; read = in.read(buffer, 0, buffer.length)) {
			expectedBytes.write(buffer, 0, read);
		}

		return expectedBytes.toByteArray();
	}
}
