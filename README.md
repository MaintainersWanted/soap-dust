# soap-dust until SOAP is deprecated

soap-dust is a SOAP client API for Java developers that need one, until SOAP is deprecated.

soap-dust does not require neither perform any code generation.

soap-dust fits within a single jar file.

soap-dust works on Android.

## How to query a remote soap server with soap-dust?

Just create a new soap-dust client. Then initialize it with the appropriate wsdl and endpoint urls. Finally, call its *call* method with the name of the remote operation you want to call and the corresponding parameters:

```java
Client client = new Client();
client.setWsdlUrl("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2?wsdl");
client.setEndPoint("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2");
ComposedValue authentication = new ComposedValue();
authentication.put("login", myLogin);
authentication.put("password", myPasswd);
client.call("login", authentication);
```

By the way, good news: this code won't work anymore since Jira deprecated its SOAP API.
    
## How do I know what the remote web-service understands?

If you do not read wsdl fluently, you can find hope in the explain method.

Just create a new soap-dust client. Initialize it with the appropriate wsdl url and call its *explain* method:

```java
Client client = new Client();
client.setWsdlUrl("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2?wsdl");
client.explain(System.out);
```
    
## How can I set a connect or read timeout in soap-dust?

soap-dust relies on standard Java *HttpURLConnection*.

If you need to customize the *HttpURLConnection* in any way and especially to set a connect and read timeout, you can do this by overriding *customizeHttpConnectionBeforeCall* in your *Client*:

```java
Client client = new Client() {
  @Override
  protected void customizeHttpConnectionBeforeCall(HttpURLConnection connection) {
    connection.setReadTimeout(1000);
    connection.setConnectTimeout(1000);
  }
};
```
    
## How can I test my code that uses soap-dust (or just code that performs HTTP requests).

soap-dust comes with a URL handler for the special protocol *test*.

A *test* url will simulate an http url. When you create this url, you set the file which contains the data you want to be returned when querying this url. You may also set the HTTP status code you want to be obtained when querying this url.

For instance querying the following url will result in a *500* HTTP response. The data received will be extracted from the file *test/response.xml*:

test:status:500;file:test/response.xml
  
You can also get the data sent to such an url at the end of your test. See *soapdust.urlhandler.test.Handler* for more information.

## How can I send an array as a ComposedValue?

As surprising as it might seem, soap-dust does not handle arrays very nicely for now :( 

To pass an array as a parameter to a soap method, you will have to either encapsulate each of your array elements inside a separate *ComposedValue* with fake names or put each element of your array in the *ComposedValue*.

For instance if the method *m* takes a parameter *p* of type array (of strings), then one can make a call like this :

```java
client.call("m", new ComposedValue().put("p", new ComposedValue()
   .put("p1", "1")
   .put("p2", "2")
   .put("p3", "3")...));
```
                
When the element is not of *xsd* type *array* but has a *maxoccur > 1*, you can call the method this way:


```java
client.call("m", new ComposedValue()
   .put("p", "1")
   .put("p", "2")
   .put("p", "3")...));
```

## soap-dust example: querying jira codehaus

Please note that this example should be updated since codehaus has terminated and Jira has deprecated its SOAP API.

Nevertheless, here we describe steps to write your first soap-dust code.

### First: write the code

Put the following code sample in a file named *QueryJiraWithSoapdust.java*:

```java
import java.io.IOException;

import soapdust.Client;
import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.MalformedWsdlException;

public class QueryJiraWithSoapdust {

    public static void main(String[] args) 
    throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
       Client client = new Client();
       client.setWsdlUrl("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2?wsdl");
       client.setEndPoint("http://jira.codehaus.org/rpc/soap/jirasoapservice-v2");
       
       String authKey = null;
       try {
           ComposedValue authentication = new ComposedValue();
           //see http://jira.codehaus.org to create an account if you dare.
           authentication.put("in0", "your login"); //put your login here
           authentication.put("in1", "your password"); //put your password here
       
           ComposedValue login = client.call("login", authentication);
       
           authKey = login.getComposedValue("loginResponse").getStringValue("loginReturn");
       } catch (FaultResponseException e) {
          if (e.fault.getComposedValue("detail").getChildrenKeys()
              .contains("com.atlassian.jira.rpc.exception.RemoteAuthenticationException")) {
              System.err.println("wrong login or password!");
              System.exit(1);
          } else {
            throw e;
        }
       }
       ComposedValue query = new ComposedValue();
       query.put("in0", authKey);
       query.put("in1", "10093");
       
       ComposedValue result = client.call("getIssuesFromFilter", query);
       
       System.out.println(result); //this will show you issues matching jira filter 10093
    }
}
```

### Second: download the last version of SOAP-dust

Check the last release of soap-dust and download it.

### Third: Compile your client

```bash
$> javac -cp soap-dust-x.y.z.jar QueryJiraWithSoapdust.java
```

### Fourth: Run your client

```bash
$> java -cp soap-dust-0.1.129.jar:. QueryJiraWithSoapdust
wrong login or password!
```

That's it !

OK... it would display a nicer output with a valid login/password. Do you really want to open an account on jira.codehaus.org? Oh wait, codehaus is terminated, we should really update this sample code ;)

And remember: your application depends on only one single jar. Useless but so fun: compare with the [cxf dependency graph](http://cxf.apache.org/docs/cxf-dependency-graphs.html).
