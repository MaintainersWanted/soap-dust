package soapdust.urlhandler.dust;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Hashtable;

import junit.framework.TestCase;
import soapdust.Client;

public class HandlerTest extends TestCase {
	@Override
	protected void setUp() throws Exception {
		new Client(); //ensures url protocols management initialization
	}
	
	public void testDustProtocolIsSUpportedByURL() throws MalformedURLException {
		new URL("dust:"); //should not fail
	}
	
	public void testOpeningADustURLReturnsAnHttpConnection() throws IOException {
		URLConnection connection = new URL("dust:").openConnection();
		
		assertTrue(connection instanceof HttpURLConnection);
	}
	
	public void testDustUrlRequestStatusCodeDefaultsTo200() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("dust:").openConnection();
		
		assertEquals(200, connection.getResponseCode());
	}

	public void testOneCanOverrideDefaultResponseCode() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("dust://status:500/").openConnection();
		
		assertEquals(500, connection.getResponseCode());
	}
	
	public void testDustServerResponseContentIsExtractedFromFile() throws IOException {
		assertUrlContent(new URL("dust://test/soapdust/urlhandler/dust/hello.txt"), 
				"Hello World !");
	}

	public void testDustServerResponseContentIsExtractedFromFileWithExplicitStatus200() throws IOException {
		assertUrlContent(new URL("dust://test/soapdust/urlhandler/dust/hello.txt"), 
				"Hello World !");
	}
	
	public void testResponseIsDuplicatedInErrorStream() throws IOException {
		assertUrlErrorStreamContent(new URL("dust://test/soapdust/urlhandler/dust/hello.txt"), 
				"Hello World !");
	}

	public void test5xxStatusThrowsIOExceptionwhenTryingToRead() throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("dust://status:500/test/soapdust/urlhandler/dust/hello.txt").openConnection();

		try {
			connection.getInputStream();
			fail();
		} catch(IOException e) {}
	}
	
	public void testOneCanWriteInADustUrl() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("dust:").openConnection();
		
		connection.getOutputStream(); //shoudl not fail
	}
	
	public void testWrittenDataAreStoredByUrlKeys() throws MalformedURLException, IOException {
		byte[] written = new byte[] {1, 2, 3, 4};
		HttpURLConnection connection = (HttpURLConnection) new URL("dust:").openConnection();
		OutputStream out = connection.getOutputStream();
		
		out.write(written);
		
		assertTrue(Arrays.equals(written, Handler.saved.get("dust:").toByteArray()));
	}
	
	//---
	private void assertUrlContent(URL url, String expectedContent) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		InputStream stream = connection.getInputStream();
		
		assertStreamContent(expectedContent, stream);
	}
	
	private void assertUrlErrorStreamContent(URL url, String expectedContent) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		InputStream stream = connection.getErrorStream();
		
		assertStreamContent(expectedContent, stream);
	}

	private void assertStreamContent(String expectedContent, InputStream stream)
			throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String result = in.readLine();
		in.close();
		
		assertEquals(result, expectedContent);
	}
}
