package soapdust.urlhandler.dust;

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

		if(urlPath.matches(STATUS_REGEX)) {
			status = Integer.parseInt(urlPath.replaceAll(STATUS_REGEX, STATUS_CAPTURE));
		} else {
			status = 200;
		}
		if (urlPath.matches(FILE_REGEX)) {
			path = urlPath.replaceAll(FILE_REGEX, FILE_CAPTURE);
		} else {
			path = null;
		}

		return new HttpURLConnection(url) {
			
			@Override
			public int getResponseCode() throws IOException {
				return status;
			}
			
			@Override
			public InputStream getInputStream() throws IOException {
				if (status >=500 && status <= 599) throw new IOException("fake server returned a fake error");
				return new FileInputStream(path);
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
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public boolean usingProxy() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void disconnect() {
				// TODO Auto-generated method stub
				
			}
		};
	}
}
