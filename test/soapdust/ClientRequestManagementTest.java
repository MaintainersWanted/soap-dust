package soapdust;

import java.io.IOException;

import junit.framework.TestCase;
import soapdust.urlhandler.test.Handler;

public class ClientRequestManagementTest extends TestCase {

	//FIXME distinguish between literal and encoded...
	
    public void testBuildXmlSoapJiraRequest() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
        Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/jira.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue authentication = new ComposedValue();
        authentication.put("in0", "login");
        authentication.put("in1", "password");

        client.call("login", authentication);

        //rpc style -> wrapping node named by the operation name
        String expected =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<sdns0:Header/>" +
            "<sdns0:Body>" +
            "<sdns1:login xmlns:sdns1=\"http://jira.codehaus.org//rpc/soap/jirasoapservice-v2\">" +
            "<sdns1:in0>login</sdns1:in0>" +
            "<sdns1:in1>password</sdns1:in1>" +
            "</sdns1:login>" +
            "</sdns0:Body>" +
            "</sdns0:Envelope>";

        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
            "<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<sdns0:Header/>" +
            "<sdns0:Body>" +
            "<sdns1:messageParameter1Element xmlns:sdns1=\"element1NS\">" +
              "<sdns1:sender>sender</sdns1:sender>" +
              "<sdns1:MSISDN>30123456789</sdns1:MSISDN>" +
              "<sdns1:IDOffre>12043</sdns1:IDOffre>" +
              "<sdns1:doscli>" +
                "<sdns2:subParameter1 xmlns:sdns2=\"schema1NS\">1</sdns2:subParameter1>" +
                "<sdns2:subParameter2 xmlns:sdns2=\"schema1NS\">2</sdns2:subParameter2>" +
                "<sdns2:subParameter3 xmlns:sdns2=\"schema1NS\">3</sdns2:subParameter3>" +
                "<sdns2:subParameter4 xmlns:sdns2=\"schema1NS\">" +
                  "<sdns1:message>coucou</sdns1:message>" +
                "</sdns2:subParameter4>" +
              "</sdns1:doscli>" +
            "</sdns1:messageParameter1Element>" + 
            "</sdns0:Body>" +
            "</sdns0:Envelope>";

        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
            "<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<sdns0:Header/>" +
            "<sdns0:Body>" +
            "<sdns1:messageParameter1Element xmlns:sdns1=\"element1NS\">" + 
            "<sdns1:sender>sender</sdns1:sender>" +
            "<sdns1:MSISDN>30123456789</sdns1:MSISDN>" +
            "<sdns1:IDOffre>12043</sdns1:IDOffre>" +
            "<sdns1:doscli xmlns:ns0=\"http://www.w3.org/2001/XMLSchema-instance\" ns0:type=\"messageSubSubParameter\">" +
              "<sdns2:subParameter1 xmlns:sdns2=\"schema1NS\">1</sdns2:subParameter1>" +
              "<sdns2:subParameter2 xmlns:sdns2=\"schema1NS\">2</sdns2:subParameter2>" +
              "<sdns2:subParameter3 xmlns:sdns2=\"schema1NS\">3</sdns2:subParameter3>" +
              "<sdns2:subParameter4 xmlns:sdns2=\"schema1NS\">" +
                "<sdns1:message>coucou</sdns1:message>" +
              "</sdns2:subParameter4>" +
            "</sdns1:doscli>" +
            "</sdns1:messageParameter1Element>" + 
            "</sdns0:Body>" +
            "</sdns0:Envelope>";

        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
        	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<sdns0:Header/>" +
        	"<sdns0:Body>" +
        	  "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
        	    "<sdns1:x>5</sdns1:x>" +
        	    "<sdns1:y>5.0</sdns1:y>" +
        	  "</sdns1:myMethod>" +
        	"</sdns0:Body>" +
        	"</sdns0:Envelope>";
        
        //FIXME expected should be the following (see xsd:int and float) : 
//        <soap:envelope>
//        <soap:body>
//            <myMethod>
//                <x xsi:type="xsd:int">5</x>
//                <y xsi:type="xsd:float">5.0</y>
//            </myMethod>
//        </soap:body>
//        </soap:envelope>
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
        	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<sdns0:Header/>" +
        	"<sdns0:Body>" +
        	  "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
        	    "<sdns1:x>5</sdns1:x>" +
        	    "<sdns1:y>5.0</sdns1:y>" +
        	  "</sdns1:myMethod>" +
        	"</sdns0:Body>" +
        	"</sdns0:Envelope>";
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
        	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<sdns0:Header/>" +
        	"<sdns0:Body>" +
              "<sdns1:xElement xmlns:sdns1=\"definitionNS\">5</sdns1:xElement>" +
              "<sdns1:yElement xmlns:sdns1=\"definitionNS\">5.0</sdns1:yElement>" +
        	"</sdns0:Body>" +
        	"</sdns0:Envelope>";
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
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
        	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<sdns0:Header/>" +
        	"<sdns0:Body>" +
        	  "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
                "<sdns1:x>5</sdns1:x>" +
                "<sdns1:y>5.0</sdns1:y>" +
              "</sdns1:myMethod>" +
        	"</sdns0:Body>" +
        	"</sdns0:Envelope>";
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
    }
    
    public void testBuildRequestWhenWsdlImportsWsdl() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
    	Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/import-wsdl.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        ComposedValue params = new ComposedValue();
        params.put("x", "5");
        params.put("y", "5.0");
        client.call("myMethod", params);
        
        String expected = 
        	"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        	"<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
        	"<sdns0:Header/>" +
        	"<sdns0:Body>" +
        	  "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
        	    "<sdns1:x>5</sdns1:x>" +
        	    "<sdns1:y>5.0</sdns1:y>" +
        	  "</sdns1:myMethod>" +
        	"</sdns0:Body>" +
        	"</sdns0:Envelope>";
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
    }
    
    public void testBuildRequestWhenSchemaUnqualified() throws IOException, MalformedWsdlException, FaultResponseException, MalformedResponseException {
        Client client = new Client();
        client.setWsdlUrl("file:test/soapdust/unqualified-type.wsdl");
        client.setEndPoint("test:file:test/soapdust/response-with-href.xml");//TODO add a response.xml file for general purpose queries

        client.call("myMethod", new ComposedValue().
                put("myMethod",
                        new ComposedValue().
                        put("x", "5").
                        put("y", "5.0")));
        
        String expected = 
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
            "<sdns0:Envelope xmlns:sdns0=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<sdns0:Header/>" +
            "<sdns0:Body>" +
              "<sdns1:myMethod xmlns:sdns1=\"definitionNS\">" +
                "<x>5</x>" +
                "<y>5.0</y>" +
              "</sdns1:myMethod>" +
            "</sdns0:Body>" +
            "</sdns0:Envelope>";
        
        assertEquals(expected, Handler.lastSaved("test:file:test/soapdust/response-with-href.xml").toString());
    }
    


}
