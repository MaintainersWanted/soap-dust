package soapdust.server;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import junit.framework.TestCase;
import soapdust.Client;
import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.MalformedWsdlException;
import soapdust.urlhandler.servlet.Handler;

public class ServletTest extends TestCase {
	private static final String REGISTERED_ACTION = "registered";
	private static final String UNREGISTERED_ACTION = "unregistered";

	private StoreHistoryHandler handler;
	private Client client;

	//FIXME in case of a malformed request, a MalformedResponseException is thrown ;{
	
	@Override
	protected void setUp() throws Exception {
		Handler.clearRegister();

		handler = new StoreHistoryHandler();

		Handler.register("soapdust", new Servlet("file:test/soapdust/server/test.wsdl").register(REGISTERED_ACTION, handler));
		
		client = new Client();
		client.setEndPoint("servlet:reg:soapdust/");
		client.setWsdlUrl("file:test/soapdust/server/test.wsdl");
		
	}

	public void testDelegatesToHandlerDependingOnSoapActionHeader() throws ServletException, IOException, FaultResponseException, MalformedResponseException {
		client.call(REGISTERED_ACTION);

		assertFalse(handler.history.isEmpty());
	}

	public void testDoesNotDelegateIfNoHandlerForAction() throws ServletException, IOException, MalformedResponseException {
		try {
			client.call(UNREGISTERED_ACTION);
		} catch (FaultResponseException e) {
			//OK
		}

		assertTrue(handler.history.isEmpty());
	}

	public void testSendsSoapFaultWhenNoHandlerForAction() throws ServletException, IOException, MalformedWsdlException, MalformedResponseException {
		//TODO check soap spec to return the exact fault in this case
		try {
			client.call(UNREGISTERED_ACTION);
			fail();
		} catch (FaultResponseException e) {
			assertEquals("Unsupported operation: " + UNREGISTERED_ACTION, e.fault.getStringValue("faultstring"));
		}
	}
	
	public void testTransmitSoapParametersToHandler() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
		ComposedValue params = new ComposedValue()
		.put("messageParameter1", new ComposedValue()
		  .put("messageParameter", new ComposedValue()
		    .put("sender", "toto")
		    .put("MSISDN", "0607080900")
		    .put("IDOffre", "12043")
		    .put("doscli", new ComposedValue()
		      .put("subParameter1", "1")
		      .put("subParameter2", "2")
		      .put("subParameter3", "3")
		      .put("subParameter4", new ComposedValue()
		        .put("message", "hello")
		        .put("untyped", "what ?")))));

		client.call(REGISTERED_ACTION, params);
		
		ComposedValue expectedParameters = new ComposedValue().put("registered", params);
		assertEquals(expectedParameters.toString(), handler.history.get(0).params.toString());
		assertEquals(expectedParameters, handler.history.get(0).params);
	}
	
	public void testSendsHandlerResponseToSoapClient() throws FaultResponseException, IOException, MalformedResponseException {
        ComposedValue expectedResponse = new ComposedValue()
          .put("sender", "sender")
          .put("MSISDN", "30123456789")
          .put("IDOffre", "12043")
          .put("doscli", new ComposedValue()
          	.put("subParameter1", "1")
          	.put("subParameter2", "2")
          	.put("subParameter3", "3")
          	.put("subParameter4", new ComposedValue()
          		.put("message", "coucou")));

		ComposedValue response = client.call(REGISTERED_ACTION);
		
		assertEquals(expectedResponse, response);
	}
	
	//---
}

class StoreHistoryHandler implements SoapDustHandler {
	final List<Call> history = new ArrayList<Call>();

	@Override
	public ComposedValue handle(String action, ComposedValue params) {
		history.add(new Call(action, params));
		return new ComposedValue()
		.put("sender", "sender")
		.put("MSISDN", "30123456789")
		.put("IDOffre", "12043")
		.put("doscli", new ComposedValue()
			.put("subParameter1", "1")
			.put("subParameter2", "2")
			.put("subParameter3", "3")
			.put("subParameter4", new ComposedValue()
				.put("message", "coucou")));
	}
}

class Call {
	final String action;
	final ComposedValue params;

	public Call(String action, ComposedValue params) {
		this.action = action;
		this.params = params;
	}
}
