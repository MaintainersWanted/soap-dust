import java.io.IOException;

import soapdust.Client;
import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.MalformedWsdlException;


public class QueryJiraWithSoapdust {

	public static void main(String[] args) throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
		Client client = new Client();
		client.setWsdlUrl("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2?wsdl");
		client.setEndPoint("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2");

		String authKey = null;
		try {
			ComposedValue authentication = new ComposedValue();
			//see http://jira.codehaus.org to create an account if you dare.
			authentication.put("login", ""); //put your login here
			authentication.put("password", ""); //put your password here

			ComposedValue login = client.call("login", authentication);

			authKey = login.getComposedValue("loginResponse").getStringValue("loginReturn");
			
		} catch (FaultResponseException e) {
			if (e.fault.getComposedValue("detail").getChildrenKeys().contains("com.atlassian.jira.rpc.exception.RemoteAuthenticationException")) {
				System.err.println("wrong login or password !");
				System.exit(1);
			} else {
				throw e;
			}
		}

		ComposedValue query = new ComposedValue();
		query.put("auth", authKey);
		query.put("filter", "10093");

		ComposedValue result = client.call("getIssuesFromFilter", query);

		System.out.println(result); //this will show you issues matching jira filter 10093
	}
}
