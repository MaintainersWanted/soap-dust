package soapdust.urlhandler.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class MockHttpServletRequest implements HttpServletRequest {

	@Override
	public Object getAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getAttributeNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCharacterEncoding() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getContentLength() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType() {
		throw new UnsupportedOperationException();
	}

	public ByteArrayOutputStream in = new ByteArrayOutputStream();
	public OutputStream tmp = new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			in.write(b);
		}
		public void close() throws IOException {
			in.close();
		};
	};
	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream input = new ByteArrayInputStream(in.toByteArray());
		return new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return input.read();
			}
		};
	}

	@Override
	public String getLocalAddr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLocalName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLocalPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getLocales() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getParameter(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map getParameterMap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Enumeration getParameterNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String[] getParameterValues(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getProtocol() {
		return "HTTP/1.1";
	}

	@Override
	public BufferedReader getReader() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRealPath(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteAddr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteHost() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getRemotePort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getScheme() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServerName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getServerPort() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAttribute(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getAuthType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContextPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Cookie[] getCookies() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getDateHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	private Map<String, List<String>> headers = new LinkedHashMap<String, List<String>>();
	public void addHeader(String name, String value) {
		List<String> values = headers.get(name);
		if (values == null) {
			values = new ArrayList<String>();
			headers.put(name, values);
		}
		values.add(value);
	}
	@Override
	public String getHeader(String name) {
		List<String> values = headers.get(name);
		return values == null ? null : values.get(0);
	}

	@Override
	public Enumeration getHeaderNames() {
		final Iterator<String> iterator = headers.keySet().iterator();
		return new Enumeration() {
			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}
			@Override
			public Object nextElement() {
				return iterator.next();
			}
		};
	}

	@Override
	public Enumeration getHeaders(String name) {
		final Iterator<String> iterator = headers.get(name).iterator();
		return new Enumeration() {
			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}
			@Override
			public Object nextElement() {
				return iterator.next();
			}
		};
	}

	@Override
	public int getIntHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	private String method;
	public MockHttpServletRequest setMethod(String method) {
		this.method = method;
		return this;
	}
	@Override
	public String getMethod() {
		return method;
	}

	@Override
	public String getPathInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPathTranslated() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteUser() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestURI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public StringBuffer getRequestURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestedSessionId() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getServletPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String arg0) {
		throw new UnsupportedOperationException();
	}

}
