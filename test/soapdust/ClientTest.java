package soapdust;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class ClientTest extends TestCase {

	public void testParseBodyWithHref() throws IOException, SAXException, ParserConfigurationException {
		Client client = new Client();

		FileInputStream inputStream = new FileInputStream("test/soapdust/response-with-href.xml");

		ComposedValue result = client.parseResponseBody(inputStream);

		ComposedValue returns = result
		.getComposedValue("getIssuesFromFilterResponse")
		.getComposedValue("getIssuesFromFilterReturn");
		assertNotNull(returns);

		ComposedValue return0 = returns.getComposedValue("getIssuesFromFilterReturn");
		assertNotNull(return0);
		assertEquals("This is test 1", return0.getStringValue("summary"));
		ComposedValue referenceToAffectVersion1 = return0.getComposedValue("affectsVersions");

		ComposedValue return1 = returns.getComposedValue("getIssuesFromFilterReturn1");
		assertNotNull(return1);
		assertEquals("This is test 2", return1.getStringValue("summary"));
		ComposedValue referenceToAffectVersion2 = return1.getComposedValue("affectsVersions");

		assertSame(referenceToAffectVersion1, referenceToAffectVersion2);
	}

	public void testEmptyNodeResultInNullStringOrComposedValueAndDoesNotFail() throws IOException, SAXException, ParserConfigurationException {
		Client client = new Client();

		FileInputStream inputStream = new FileInputStream("test/soapdust/response-with-empty-nodes.xml");

		ComposedValue result = client.parseResponseBody(inputStream);

		ComposedValue returns = result
		.getComposedValue("getIssuesFromFilterResponse")
		.getComposedValue("getIssuesFromFilterReturn");
		assertNotNull(returns);

		ComposedValue version = returns.getComposedValue("affectsVersions");
		assertNull(version.getStringValue("name"));
		assertNull(version.getComposedValue("name"));
	}

	public void testCreateFaultException() throws IOException, FaultResponseException, SAXException, ParserConfigurationException {
		Client client = new Client() {
			@Override
			InputStream errorStream(HttpURLConnection connection)
					throws IOException {
				return new FileInputStream("test/soapdust/response-with-fault.xml");
			}
			
			@Override
			int responseCode(HttpURLConnection connection) throws IOException {
				return 500;
			}
		};
		ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
		{
			FileInputStream in = new FileInputStream("test/soapdust/response-with-fault.xml");
			byte[] buffer = new byte[1024];
			for(int read = in.read(buffer, 0, buffer.length); read != -1; read = in.read(buffer, 0, buffer.length)) {
				expectedBytes.write(buffer, 0, read);
			}
		}


		FaultResponseException faultException = client.createFaultException(null);

		assertEquals("soapenv:Server.userException", faultException.fault.getStringValue("faultcode"));
		assertEquals("com.atlassian.jira.rpc.exception.RemoteAuthenticationException: Invalid username or password.", faultException.fault.getStringValue("faultstring"));
		assertEquals(500, faultException.responseCode);
		assertTrue(Arrays.equals(expectedBytes.toByteArray(), faultException.response));
	}

	public void testRegisterDataWhenUnhandledHttpStatusCodeReturned() throws IOException {
		Client client = new Client() {
			@Override
			InputStream inputStream(HttpURLConnection connection)
					throws IOException {
				return new FileInputStream("test/soapdust/response-with-fault.xml");
			}
			
			@Override
			int responseCode(HttpURLConnection connection) throws IOException {
				return 502;
			}
		};
		ByteArrayOutputStream expectedBytes = new ByteArrayOutputStream();
		{
			FileInputStream in = new FileInputStream("test/soapdust/response-with-fault.xml");
			byte[] buffer = new byte[1024];
			for(int read = in.read(buffer, 0, buffer.length); read != -1; read = in.read(buffer, 0, buffer.length)) {
				expectedBytes.write(buffer, 0, read);
			}
		}


		try {
			client.handleResponseCode(null);
		} catch(MalformedResponseException e) {
			assertEquals(502, e.responseCode);
			assertTrue(Arrays.equals(expectedBytes.toByteArray(), e.response));
		}
	}

	//TODO the 2 following tests are slow... check why and fix this
	public void testBuildXmlSoapJiraRequest() throws IOException, MalformedWsdlException, FaultResponseException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		Client client = new Client() {
			@Override
			OutputStream outputStream(HttpURLConnection connection)
			throws IOException {
				return out;
			}
			@Override
			InputStream inputStream(HttpURLConnection connection)
			throws IOException {
				return null;
			}
			@Override
			int responseCode(HttpURLConnection connection) throws IOException {
				return 200;
			}
			@Override
			ComposedValue readResponse(HttpURLConnection connection)
					throws FaultResponseException, IOException, SAXException,
					ParserConfigurationException, MalformedResponseException {
				return null;
			}
		};
		client.setEndPoint("http://localhost/");
		client.setWsdlUrl("file:test/soapdust/jira.wsdl");

		ComposedValue authentication = new ComposedValue();
		authentication.put("login", "login"); //put your login here
		authentication.put("password", "password"); //put your password here

		try {
			client.call("login", authentication);
		} catch (MalformedResponseException e) {
			//ignore
		}

		String expected =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
			"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
			"<Header/>" +
			"<Body>" +
			"<login xmlns=\"http://jira.codehaus.org//rpc/soap/jirasoapservice-v2\">" +
			"<login>login</login>" +
			"<password>password</password>" +
			"</login>" +
			"</Body>" +
			"</Envelope>";

		assertEquals(expected, out.toString());
	}

	public void testBuildXmlSoapTestRequest() throws IOException, MalformedWsdlException, FaultResponseException {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		Client client = new Client() {
			@Override
			OutputStream outputStream(HttpURLConnection connection)
			throws IOException {
				return out;
			}
			@Override
			InputStream inputStream(HttpURLConnection connection)
			throws IOException {
				return null;
			}
			@Override
			int responseCode(HttpURLConnection connection) throws IOException {
				return 200;
			}
			@Override
			ComposedValue readResponse(HttpURLConnection connection)
					throws FaultResponseException, IOException, SAXException,
					ParserConfigurationException, MalformedResponseException {
				return null;
			}
		};
		client.setEndPoint("http://localhost/");
		client.setWsdlUrl("file:test/soapdust/test.wsdl");

		ComposedValue messageParameter1 = new ComposedValue();
		messageParameter1.put("sender", "sender");
		messageParameter1.put("MSISDN", "30123456789");
		messageParameter1.put("IDOffre", "12043");
		{
			ComposedValue dosCli = new ComposedValue();
			dosCli.put("subParameter1", "1");
			dosCli.put("subParameter2", "2");
			dosCli.put("subParameter3", "3");
			{
				ComposedValue subParameter4 = new ComposedValue();
				subParameter4.put("message", "coucou");
				dosCli.put("subParameter4", subParameter4);
			}
			messageParameter1.put("doscli", dosCli);
		}

		try {
			//TODO handle multi-part messages
			ComposedValue result = client.call("testOperation1", messageParameter1);
		} catch (MalformedResponseException e) {
			//ignore
		}

		String expected =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
			"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
			"<Header/>" +
			"<Body>" +
			"<testOperation1 xmlns=\"definitionNS\">" +
			"<sender xmlns=\"element1NS\">sender</sender>" +
			"<MSISDN xmlns=\"element1NS\">30123456789</MSISDN>" +
			"<IDOffre xmlns=\"element1NS\">12043</IDOffre>" +
			"<doscli xmlns=\"element1NS\">" +
			"<subParameter1 xmlns=\"schema1NS\">1</subParameter1>" +
			"<subParameter2 xmlns=\"schema1NS\">2</subParameter2>" +
			"<subParameter3 xmlns=\"schema1NS\">3</subParameter3>" +
			"<subParameter4 xmlns=\"schema1NS\">" +
			"<message xmlns=\"element1NS\">coucou</message>" +
			"</subParameter4>" +
			"</doscli>" +
			"</testOperation1>" +
			"</Body>" +
			"</Envelope>";

		assertEquals(expected, out.toString());
	}
}
