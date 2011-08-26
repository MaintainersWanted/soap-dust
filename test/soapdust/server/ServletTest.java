package soapdust.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import soapdust.ComposedValue;

public class ServletTest extends TestCase {
	private static final String REGISTERED_ACTION = "registered";
	private static final String UNREGISTERED_ACTION = "unregistered";
	
	private Servlet servlet;
	private StoreHistoryHandler handler;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;

	@Override
	protected void setUp() throws Exception {
		servlet = new Servlet();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();

		handler = new StoreHistoryHandler();
		servlet.register(REGISTERED_ACTION, handler);
	}
	
	public void testDoPostDelegateToHandlerDependingOnSoapActionHeader() throws ServletException, IOException {
		servlet.doPost(request.addHeader("SOAPAction", REGISTERED_ACTION), null);
		
		assertFalse(handler.history.isEmpty());
	}
	
	public void testDoPostDoesNotDelegateIfNoHandlerForAction() throws ServletException, IOException {
		servlet.doPost(request.addHeader("SOAPAction", UNREGISTERED_ACTION), null);
		
		assertTrue(handler.history.isEmpty());
	}
	
	public void testDoPostStatus500WhenNoHandlerForAction() throws ServletException, IOException {
		servlet.doPost(new MockHttpServletRequest().addHeader("SOAPAction", UNREGISTERED_ACTION), null);
		
		assertEquals(500, response.status);
	}

}

class StoreHistoryHandler implements SoapDustHandler {
	final List<Call> history = new ArrayList<Call>();

	@Override
	public ComposedValue handle(String action, ComposedValue params) {
		history.add(new Call(action, params));
		return null;
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
