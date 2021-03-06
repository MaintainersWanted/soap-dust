package soapdust.urlhandler.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class MockHttpServletResponse implements HttpServletResponse {

	@Override
	public void flushBuffer() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getBufferSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCharacterEncoding() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Locale getLocale() {
		throw new UnsupportedOperationException();
	}

	public ByteArrayOutputStream out = new ByteArrayOutputStream();
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				out.write(b);
			}
		};
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCommitted() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void resetBuffer() {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setBufferSize(int arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setCharacterEncoding(String arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setContentLength(int arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setContentType(String contentType) {
		//TODO
	}

	@Override
	public void setLocale(Locale arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addCookie(Cookie arg0) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addHeader(String arg0, String arg1) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean containsHeader(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeURL(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeUrl(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void sendError(int arg0) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void sendError(int sc, String message) throws IOException {
		this.status = sc;
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setHeader(String arg0, String arg1) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		throw new UnsupportedOperationException();

	}

	public int status = 200; //success by default
	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void setStatus(int arg0, String arg1) {
		throw new UnsupportedOperationException();

	}

}
