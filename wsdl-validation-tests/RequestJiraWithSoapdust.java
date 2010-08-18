import java.io.IOException;

import junit.framework.TestCase;
import soapdust.Client;
import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.MalformedWsdlException;


public class RequestJiraWithSoapdust extends TestCase {

	public void testJira() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
		Client client = new Client();
		client.setWsdlUrl("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2?wsdl");
		client.setEndPoint("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2");

		String authKey = null;
		try {
			ComposedValue authentication = new ComposedValue();
			authentication.put("login", "login"); //put your login here
			authentication.put("password", "password"); //put your password here

			ComposedValue login = client.call("login", authentication);

			authKey = login.getComposedValue("loginResponse").getStringValue("loginReturn");
			
		} catch (FaultResponseException e) {
			if (e.fault.getComposedValue("detail").getChildrenKeys().contains("com.atlassian.jira.rpc.exception.RemoteAuthenticationException")) {
				//login error is OK
				System.err.println("wrong login or password !");
				return;
			} else {
				throw e;
			}
		}

		ComposedValue query = new ComposedValue();
		query.put("auth", authKey);
		query.put("filter", "10093");

		ComposedValue result = client.call("getIssuesFromFilter", query);

		System.out.println(result);
	}

}
