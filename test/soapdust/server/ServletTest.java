package soapdust.server;

import java.io.IOException;
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
	private static final String REGISTERED_ACTION_SOAP_ACTION = "registeredaction";
	private static final String UNREGISTERED_ACTION = "unregistered";

	private StoreHistoryHandler handler;
	private Client client;
	private Servlet servlet;

	//FIXME in case of a malformed request, a MalformedResponseException is thrown ;{
	
	//TODO find a way to guess the action called.
	//     if it is a rpc style operation: use the enclosing xml node name (since soapAction is sometimes shared...)
	//     if it is a document style operation:
	//         if is not wrapped: use soapAction header
	//         if it is wrapped: use wrapping node name
	
//	Detect document-wrapped wsdl:
//	Extracted from ExamplesWSDL.html
//	  * The input message has a single part.
//    * The part is an element.
//    * The element has the same name as the operation.
//    * The element's complex type has no attributes.

	
	@Override
	protected void setUp() throws Exception {
		Handler.clearRegister();

		handler = new StoreHistoryHandler();

		servlet = new Servlet().setWsdl("file:test/soapdust/server/test.wsdl").register(REGISTERED_ACTION_SOAP_ACTION, handler);
		Handler.register("soapdust", servlet);
		
		client = new Client();
		client.setEndPoint("servlet:reg:soapdust/");
		client.setWsdlUrl("file:test/soapdust/server/test.wsdl");
		
	}

	public void tesDelegatesToHandlerDependingOnSoapActionHeader() throws ServletException, IOException, FaultResponseException, MalformedResponseException {
		client.call(REGISTERED_ACTION);

		assertFalse(handler.history.isEmpty());
	}

	public void tesDoesNotDelegateIfNoHandlerForAction() throws ServletException, IOException, MalformedResponseException {
		try {
			client.call(UNREGISTERED_ACTION);
		} catch (FaultResponseException e) {
			//OK
		}

		assertTrue(handler.history.isEmpty());
	}

	public void tesSendsSoapFaultWhenNoHandlerForAction() throws ServletException, IOException, MalformedWsdlException, MalformedResponseException {
		//TODO check soap spec to return the exact fault in this case
		try {
			client.call(UNREGISTERED_ACTION);
			fail();
		} catch (FaultResponseException e) {
			assertEquals("Unsupported operation: " + UNREGISTERED_ACTION, e.fault.getStringValue("faultstring"));
		}
	}
	
	public void tesTransmitSoapParametersToHandler() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
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
		assertEquals(expectedParameters, handler.history.get(0).params);
	}
	
	public void tesSendsHandlerResponseToSoapClient() throws FaultResponseException, IOException, MalformedResponseException {
        ComposedValue expectedResponse = new ComposedValue()
		.put("messageResponse1", new ComposedValue()
            .put("sender", "sender")
            .put("MSISDN", "30123456789")
            .put("IDOffre", "12043")
            .put("doscli", new ComposedValue()
                .put("subParameter1", "1")
                .put("subParameter2", "2")
                .put("subParameter3", "3")
                .put("subParameter4", new ComposedValue()
            	.put("message", "coucou"))));
        servlet.register(REGISTERED_ACTION_SOAP_ACTION, new ReturnResponseHandler());

		ComposedValue response = client.call(REGISTERED_ACTION);
		
		assertEquals(expectedResponse.toString(), response.toString());
		assertEquals(expectedResponse, response);
	}
	
	public void testReturnSoapFaultToClient() {
		//TODO
	}
	
	//---
}

class StoreHistoryHandler implements SoapDustHandler {
	final List<Call> history = new ArrayList<Call>();

	@Override
	public ComposedValue handle(String action, ComposedValue params) {
		history.add(new Call(action, params));
		return null;
	}
}

class ReturnResponseHandler implements SoapDustHandler {
	@Override
	public ComposedValue handle(String action, ComposedValue params) {
		return new ComposedValue()
		.put("messageResponse1", new ComposedValue()
			.put("sender", "sender")
			.put("MSISDN", "30123456789")
			.put("IDOffre", "12043")
			.put("doscli", new ComposedValue()
				.put("subParameter1", "1")
				.put("subParameter2", "2")
				.put("subParameter3", "3")
				.put("subParameter4", new ComposedValue()
					.put("message", "coucou"))));
	}
}

class ThrowsFaultExceptionHandler implements SoapDustHandler {
	@Override
	public ComposedValue handle(String action, ComposedValue params) throws FaultResponseException {
		throw new FaultResponseException(new ComposedValue());//TODO
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
