import java.io.IOException;

import junit.framework.TestCase;
import soapdust.Client;
import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.MalformedWsdlException;


public class RequestFoxCentralWithSoapdust extends TestCase {

	public void testFox() throws IOException, MalformedWsdlException, MalformedResponseException {
		Client client = new Client();
		client.setWsdlUrl("http://www.foxcentral.net/foxcentral.wsdl");
		client.setEndPoint("http://www.foxcentral.net/foxcentral.asmx");
//		client.setEndPoint("http://localhost:8080/axis2/services/FoxCentral");
		
		try {
			ComposedValue result = client.call("GetArticles");
			String articles = result.getComposedValue("GetArticlesResponse").getStringValue("GetArticlesResult");
			assertNotNull(articles);
			System.out.println(articles);

		} catch (FaultResponseException e) {
			//that's OK if that's our generated server
			assertEquals("soapenv:Server", e.fault.getStringValue("faultcode"));
			assertEquals("Please implement com.west_wind.www.foxcentral.FoxCentralSkeleton#getArticles", e.fault.getStringValue("faultstring"));
			e.printStackTrace();
		}
	}
}
