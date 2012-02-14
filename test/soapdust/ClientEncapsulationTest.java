package soapdust;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

public class ClientEncapsulationTest extends TestCase {

	private String request;
	private String response;
	
	public void testEncapsulateOutput() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
		Client client = new EncapsulatedClient();
        client.setWsdlUrl("file:test/soapdust/rpc-literal.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        client.call("myMethod", new ComposedValue()
          .put("x", "5")
          .put("y", "5.0"));

        String expected = 
            	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            	"<sdns0:Header/>" +
            	"<sdns0:Body>" +
            	  "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
            	    "<sdns1:x>5</sdns1:x>" +
            	    "<sdns1:y>5.0</sdns1:y>" +
            	  "</sdns1:myMethod>" +
            	"</sdns0:Body>" +
            	"</sdns0:Envelope>";

        assertEquals(expected, request);
	}
	
	public void testEncapsulateInput() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {

		Client client = new EncapsulatedClient();
		client.setWsdlUrl("file:test/soapdust/rpc-literal.wsdl");
		client.setEndPoint("test:file:test/soapdust/empty-response.xml");
		
        client.call("myMethod", new ComposedValue()
          .put("x", "5")
          .put("y", "5.0"));

        String expected =
        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
        		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" + "\n" + 
                "                  xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\"" + "\n" +
                "                  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\">" + "\n" +
                "<soapenv:Body>" + "\n" +
                "  <soapenv:Fault/>" + "\n" +
                "</soapenv:Body>" + "\n" +
                "</soapenv:Envelope>" + "\n";

		assertEquals(expected, response);
	}
	
	public void testEncapsulateErrorInput() throws FaultResponseException, IOException, MalformedResponseException, MalformedWsdlException {
		Client client = new EncapsulatedClient();
		client.setWsdlUrl("file:test/soapdust/rpc-literal.wsdl");
		client.setEndPoint("test:status:500;file:test/soapdust/empty-response.xml");
		
		try {
			client.call("myMethod", new ComposedValue()
			.put("x", "5")
			.put("y", "5.0"));
		} catch(FaultResponseException e) {
			//that's OK
		}

        String expected =
        		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n" +
        		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" + "\n" + 
                "                  xmlns:xsd=\"http://www.w3.org/1999/XMLSchema\"" + "\n" +
                "                  xmlns:xsi=\"http://www.w3.org/1999/XMLSchema-instance\">" + "\n" +
                "<soapenv:Body>" + "\n" +
                "  <soapenv:Fault/>" + "\n" +
                "</soapenv:Body>" + "\n" +
                "</soapenv:Envelope>" + "\n";

		assertEquals(expected, response);
	}
	
	private class EncapsulatedClient extends Client {
		@Override
		protected OutputStream encapsulate(OutputStream target) {
			return new FilterOutputStream(target) {
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				@Override
				public void write(int b) throws IOException {
					data.write(b);
					out.write(b);
				}
				@Override
				public void close() throws IOException {
					request = data.toString();
					out.close();
				}
			};
		}
		
		@Override
		protected InputStream encapsulate(InputStream source) {
			return new FilterInputStream(source) {
				ByteArrayOutputStream data = new ByteArrayOutputStream();
				@Override
				public int read(byte[] b, int off, int len)
						throws IOException {
					int red = in.read(b, off, len);
					if (red != -1) data.write(b, off, red);
					return red;
				}
				@Override
				public int read() throws IOException {
					int red = in.read();
					if (red != -1) data.write(red);
					return red;
				}
				@Override
				public void close() throws IOException {
					response = data.toString();
					in.close();
				}
			};
		}
	}
}