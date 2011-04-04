package soapdust;

import java.io.IOException;

import junit.framework.TestCase;
import soapdust.urlhandler.test.Handler;

public class ClientRequestManagementTest extends TestCase {

    // the 2 following tests are slow... because jira.wsdl takes time to parse
    public void testBuildXmlSoapJiraRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
        Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/jira.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue authentication = new ComposedValue();
        authentication.put("login", "login");
        authentication.put("password", "password");

        client.call("login", authentication);

        //rpc style -> wrapping node named by the operation name
        String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<Header/>" +
            "<Body>" +
            "<login xmlns=\"http://jira.codehaus.org//rpc/soap/jirasoapservice-v2\">" +
            "<login>login</login>" +
            "<password>password</password>" +
            "</login>" +
            "</Body>" +
            "</Envelope>";

        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }


    public void testBuildXmlSoapTestRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
        Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/test.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue messageParameter1 = new ComposedValue();
        messageParameter1.put("sender", "sender");
        messageParameter1.put("MSISDN", "30123456789");
        messageParameter1.put("IDOffre", "12043");
        {
            ComposedValue dosCli = new ComposedValue();
            dosCli.put("subParameter1", "1");
            dosCli.put("subParameter2", "2");
            dosCli.put("subParameter3", "3");
            {
                ComposedValue subParameter4 = new ComposedValue();
                subParameter4.put("message", "coucou");
                dosCli.put("subParameter4", subParameter4);
            }
            messageParameter1.put("doscli", dosCli);
        }

        ComposedValue testOperation1 = new ComposedValue().put("messageParameter1Element", messageParameter1);

        //TODO handle multi-part messages
        client.call("testOperation1", testOperation1);

        //document style -> no wrapping node
        String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<Header/>" +
            "<Body>" +
            "<messageParameter1Element xmlns=\"definitionNS\">" + //FIXME should be xmlns=\"Element1NS\">
              "<sender xmlns=\"element1NS\">sender</sender>" +
              "<MSISDN xmlns=\"element1NS\">30123456789</MSISDN>" +
              "<IDOffre xmlns=\"element1NS\">12043</IDOffre>" +
              "<doscli xmlns=\"element1NS\">" +
                "<subParameter1 xmlns=\"schema1NS\">1</subParameter1>" +
                "<subParameter2 xmlns=\"schema1NS\">2</subParameter2>" +
                "<subParameter3 xmlns=\"schema1NS\">3</subParameter3>" +
                "<subParameter4 xmlns=\"schema1NS\">" +
                "<message xmlns=\"element1NS\">coucou</message>" +
                "</subParameter4>" +
              "</doscli>" +
            "</messageParameter1Element>" + 
            "</Body>" +
            "</Envelope>";

        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }

    public void testBuildXmlSoapTestRequestWithSpecificType() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
        Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/test.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue messageParameter1 = new ComposedValue();
        messageParameter1.put("sender", "sender");
        messageParameter1.put("MSISDN", "30123456789");
        messageParameter1.put("IDOffre", "12043");
        {
            ComposedValue dosCli = new ComposedValue();
            dosCli.type = "messageSubSubParameter";
            dosCli.put("subParameter1", "1");
            dosCli.put("subParameter2", "2");
            dosCli.put("subParameter3", "3");
            {
                ComposedValue subParameter4 = new ComposedValue();
                subParameter4.put("message", "coucou");
                dosCli.put("subParameter4", subParameter4);
            }
            messageParameter1.put("doscli", dosCli);
        }

        //TODO handle multi-part messages
        client.call("testOperation1", 
        		new ComposedValue().put("messageParameter1Element", messageParameter1));

        //document style -> no wrapping node
        String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<Header/>" +
            "<Body>" +
            "<messageParameter1Element xmlns=\"definitionNS\">" + //FIXME should be xmlns=\"Element1NS\">
            "<sender xmlns=\"element1NS\">sender</sender>" +
            "<MSISDN xmlns=\"element1NS\">30123456789</MSISDN>" +
            "<IDOffre xmlns=\"element1NS\">12043</IDOffre>" +
            "<doscli xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\" ns0:type=\"messageSubSubParameter\" xmlns=\"element1NS\">" +
              "<subParameter1 xmlns=\"schema1NS\">1</subParameter1>" +
              "<subParameter2 xmlns=\"schema1NS\">2</subParameter2>" +
              "<subParameter3 xmlns=\"schema1NS\">3</subParameter3>" +
              "<subParameter4 xmlns=\"schema1NS\">" +
                "<message xmlns=\"element1NS\">coucou</message>" +
              "</subParameter4>" +
            "</doscli>" +
            "</messageParameter1Element>" + 
            "</Body>" +
            "</Envelope>";

        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }
    
    public void testBuildRpcEncodedRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/rpc-encoded.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue params = new ComposedValue();
        params.put("x", "5");
        params.put("y", "5.0");
        client.call("myMethod", params);
        
        String expected = 
        	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        	"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<Header/>" +
        	"<Body>" +
        	  "<myMethod xmlns=\"definitionNS\">" +
        	    "<x>5</x>" +
        	    "<y>5.0</y>" +
        	  "</myMethod>" +
        	"</Body>" +
        	"</Envelope>";
        
        //FIXME expected should be the following (see xsd:int and float) : 
//        <soap:envelope>
//        <soap:body>
//            <myMethod>
//                <x xsi:type="xsd:int">5</x>
//                <y xsi:type="xsd:float">5.0</y>
//            </myMethod>
//        </soap:body>
//        </soap:envelope>
        
        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }

    public void testBuildRpcLiteralRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/rpc-literal.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue params = new ComposedValue();
        params.put("x", "5");
        params.put("y", "5.0");
        client.call("myMethod", params);
        
        String expected = 
        	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        	"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<Header/>" +
        	"<Body>" +
        	  "<myMethod xmlns=\"definitionNS\">" +
        	    "<x>5</x>" +
        	    "<y>5.0</y>" +
        	  "</myMethod>" +
        	"</Body>" +
        	"</Envelope>";
        
        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }
    
    public void testBuildDocumentEncodedRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	// Nobody follows this style. It is not WS-I compliant. So let's move on.
    	// See http://www.ibm.com/developerworks/webservices/library/ws-whichwsdl/
    }
    
    public void testBuildDocumentLiteralRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/document-literal.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue params = new ComposedValue();
        params.put("xElement", "5");
        params.put("yElement", "5.0");
        client.call("myMethod", params);
        
        String expected = 
        	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        	"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<Header/>" +
        	"<Body>" +
              "<xElement xmlns=\"definitionNS\">5</xElement>" +
              "<yElement xmlns=\"definitionNS\">5.0</yElement>" +
        	"</Body>" +
        	"</Envelope>";
        
        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }
    
    public void testBuildDocumentLiteralWrappedRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/document-literal-wrapped.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        client.call("myMethod", new ComposedValue().
        		put("myMethod",
        				new ComposedValue().
        				put("x", "5").
        				put("y", "5.0")));
        
        String expected = 
        	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        	"<Envelope xmlns=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<Header/>" +
        	"<Body>" +
        	  "<myMethod xmlns=\"definitionNS\">" +
                "<x>5</x>" +
                "<y>5.0</y>" +
              "</myMethod>" +
        	"</Body>" +
        	"</Envelope>";
        
        assertEquals(expected, Handler.saved.get("test:file:test/soapdust/response-with-href.xml").toString());
    }
    


}
