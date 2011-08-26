package soapdust.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import soapdust.ComposedValue;

public class Servlet extends HttpServlet {
	private Map<String, SoapDustHandler> handlers = new HashMap<String, SoapDustHandler>();

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String action = req.getHeader("SOAPAction");
		SoapDustHandler handler = handlers.get(action);
		handler = handler == null ? new DefaultHandler() : handler;
		handler.handle(action, null);
	}

	public void register(String operation, SoapDustHandler handler) {
		handlers.put(operation, handler);
	}
}

class DefaultHandler implements SoapDustHandler {
	@Override
	public ComposedValue handle(String action, ComposedValue params) {
		return null;
	}
}
