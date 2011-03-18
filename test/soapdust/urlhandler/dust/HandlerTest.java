package soapdust.urlhandler.dust;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;

import junit.framework.TestCase;
import soapdust.Client;
import soapdust.urlhandler.test.Handler;

public class HandlerTest extends TestCase {
	private static final byte[] TEST_DATA = new byte[] {0, 1, 2, 3};
	private static final String TEST_FILE = System.getProperty("java.io.tmpdir") + File.separator + "test_file";
	
	@Override
	protected void setUp() throws Exception {
		new Client(); //ensures url protocols management initialization
		writeFile(TEST_FILE, TEST_DATA);
	}
	

	public void testDustProtocolIsSUpportedByURL() throws MalformedURLException {
		new URL("test:"); //should not fail
	}
	
	public void testOpeningADustURLReturnsAnHttpConnection() throws IOException {
		URLConnection connection = new URL("test:").openConnection();
		
		assertTrue(connection instanceof HttpURLConnection);
	}
	
	public void testDustUrlRequestStatusCodeDefaultsTo200() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("test:").openConnection();
		
		assertEquals(200, connection.getResponseCode());
	}

	public void testOneCanOverrideDefaultResponseCode() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("test:status:500").openConnection();
		
		assertEquals(500, connection.getResponseCode());
	}

	public void testDustServerResponseIsEmptyWhenNoFileSpecified() throws IOException {
		assertUrlContent(new URL("test:"), new byte[0]);
	}
	
	public void testDustServerResponseContentIsExtractedFromFile() throws IOException {
		assertUrlContent(new URL("test:file:" + TEST_FILE), TEST_DATA);
	}

	public void testDustServerResponseContentIsExtractedFromFileWithExplicitStatus200() throws IOException {
		assertUrlContent(new URL("test:status:200;file:" + TEST_FILE), TEST_DATA);
	}
	
	public void test5xxStatusThrowsIOExceptionwhenTryingToRead() throws MalformedURLException, IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("test:status:500;file:test/soapdust/urlhandler/dust/hello.txt").openConnection();

		try {
			connection.getInputStream();
			fail();
		} catch(IOException e) {}
	}

	public void testCanReadResponseFromErrorStreamWhen5xxStatus() throws MalformedURLException, IOException {
		assertUrlErrorStreamContent(new URL("test:status:500;file:" + TEST_FILE), TEST_DATA);
	}

//TODO
//	public void testCanNotReadResponseFromErrorStreamWhenNot5xxStatus() throws IOException {
//		HttpURLConnection connection = (HttpURLConnection) new URL("test:file:" + TEST_FILE).openConnection();
//		
//		assertNull(connection.getErrorStream());
//	}
	
	public void testOneCanWriteInADustUrl() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) new URL("test:").openConnection();
		
		connection.getOutputStream(); //shoudl not fail
	}
	
	public void testWrittenDataAreStoredByUrlKeys() throws MalformedURLException, IOException {
		byte[] written = new byte[] {1, 2, 3, 4};
		HttpURLConnection connection = (HttpURLConnection) new URL("test:").openConnection();
		OutputStream out = connection.getOutputStream();
		
		out.write(written);
		
		assertTrue(Arrays.equals(written, Handler.saved.get("test:").toByteArray()));
	}
	
	//---

	private void assertUrlContent(URL url, byte[] expectedContent) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		InputStream stream = connection.getInputStream();
		
		assertStreamContent(expectedContent, stream);
	}

	private void assertUrlErrorStreamContent(URL url, byte[] expectedContent) throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		InputStream stream = connection.getErrorStream();
		
		assertStreamContent(expectedContent, stream);
	}

	private void assertStreamContent(byte[] expectedContent, InputStream stream)
	throws IOException {
		assertTrue(Arrays.equals(expectedContent, readFully(stream)));
	}

	private byte[] readFully(InputStream in) throws IOException {
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		
		for(int read = in.read(buffer, 0, buffer.length); read != -1; read = in.read(buffer, 0, buffer.length)) {
			content.write(buffer, 0, read);
		}

		return content.toByteArray();
	}

	private void writeFile(String file, byte[] data) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		out.write(data);
		out.flush();
	}
}
