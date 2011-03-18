package soapdust.urlhandler.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Hashtable;

/**
 * This class handles test: urls. The jvm is automatically initialized
 * so that this class is used to resolve test: urls.
 * 
 * test: urls allows you to test an application that access to a
 * http server by pointing your application to a test: url instead
 * of a http: url.
 * 
 * With a test: url, you can specify the http status code you want the
 * http request to return and also a file which content will be
 * returned as the http response content or error content.
 * 
 * test: may be of the form :
 * 
 * test:status:500 => requesting this url will result in a 500 http status
 * 
 * test:file:test/response.xml => requesting this url will return the content of the given file
 * 
 * test:status:500;file:test/response.xml
 * 
 * status: is optionnal and defaults to 200
 * file: if optionnal and defaults to empty file
 *
 * One can consult the data written "to" a test: url by accessing the public
 * HashTable saved in this class. Data is indexed by url.
 * 
 * See HandlerTest.java for examples of using this class.
 *
 */
public class Handler extends URLStreamHandler {
	private static final String STATUS_CAPTURE = "$2";
	private static final String FILE_CAPTURE = "$2";
	private static final String FILE_REGEX = "(|.*;)file:([^;]*).*";
	private static final String STATUS_REGEX = "(|.*;)status:([^;]*).*";
	
	public static Hashtable<String, ByteArrayOutputStream> saved = new Hashtable<String, ByteArrayOutputStream>();

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		final String urlPath = url.getPath();
		final int status;
		final String path;

		String statusAsString = extractValue(urlPath, STATUS_REGEX, STATUS_CAPTURE);
		status = statusAsString == null ? 200 : Integer.parseInt(statusAsString);
		path = extractValue(urlPath, FILE_REGEX, FILE_CAPTURE);

		return new HttpURLConnection(url) {
			
			@Override
			public int getResponseCode() throws IOException {
				return status;
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				if (status >=500 && status <= 599) throw new IOException("fake server returned a fake error");
				if (path == null) {
					return new ByteArrayInputStream(new byte[0]);
				} else {
					return new FileInputStream(path);
				}
			}
			
			@Override
			public InputStream getErrorStream() {
				try {
					return new FileInputStream(path);
				} catch (FileNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			
			@Override
			public OutputStream getOutputStream() throws IOException {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Handler.saved.put(url.toString(), out);
				return out;
			}
			
			@Override
			public String getContentType() {
				return "test/plain";
			}
			
			@Override
			public void connect() throws IOException {
			}
			
			@Override
			public boolean usingProxy() {
				return false;
			}
			
			@Override
			public void disconnect() {
			}
		};
	}

	private String extractValue(final String urlPath, String regex, String capture) {
		if (urlPath.matches(regex)) {
			return urlPath.replaceAll(regex, capture);
		} else {
			return null;
		}
	}
}
