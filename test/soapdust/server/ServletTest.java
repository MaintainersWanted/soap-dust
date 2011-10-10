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
	private static final String RPC_OPERATION = "rpcoperation";
	private static final String DOCUMENT_OPERATION = "documentoperation";
	private static final String DOCUMENT_WRAPPED_OPERATION = "messageParameter";
	private static final String DOCUMENT_WRAPPED_OPERATION_2 = "messageSubSubParameter";
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

		servlet = new Servlet().setWsdl("file:test/soapdust/server/test.wsdl");
		servlet.register(REGISTERED_ACTION, handler);
		
		Handler.register("soapdust", servlet);
		
		client = new Client();
		client.setEndPoint("servlet:reg:soapdust/");
		client.setWsdlUrl("file:test/soapdust/server/test.wsdl");
		
	}

	public void testDelegatesToHandlerForRpcStyle() throws ServletException, IOException, FaultResponseException, MalformedResponseException {
		servlet.register(RPC_OPERATION, handler);
		
		client.call(RPC_OPERATION);

		assertFalse(handler.history.isEmpty());
	}
	
	public void testDelegatesToHandlerForDocumentStyle() throws ServletException, IOException, FaultResponseException, MalformedResponseException {
		servlet.register(DOCUMENT_OPERATION, handler);
		
		client.call(DOCUMENT_OPERATION);

		assertFalse(handler.history.isEmpty());
	}
	
	public void testDelegatesToCorrectHandlerForDocumentWrappedStyle() throws ServletException, IOException, FaultResponseException, MalformedResponseException {
		servlet.register(DOCUMENT_WRAPPED_OPERATION, handler);
		servlet.register(DOCUMENT_WRAPPED_OPERATION_2, new ThrowsFaultExceptionHandler()); //bad handler
		
		client.call(DOCUMENT_WRAPPED_OPERATION);

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
		assertEquals(expectedParameters, handler.history.get(0).params);
	}
	
	public void testSendsHandlerResponseToSoapClient() throws FaultResponseException, IOException, MalformedResponseException {
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
        servlet.register(REGISTERED_ACTION, new ReturnResponseHandler());

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
