package soapdust.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class WsdlParserOperationPartTest extends TestCase {
	
	public void testAssociateOperationsWithDefinition() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertNotNull(result.getDefinition("definitionNS").operations.get("testOperation1"));
		assertNotNull(result.getDefinition("definitionNS").operations.get("testOperation2"));
	}
	
	public void testAssociateNameWithOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertEquals("testOperation1", (result.getDefinition("definitionNS").operations.get("testOperation1").name));
		assertEquals("testOperation2", (result.getDefinition("definitionNS").operations.get("testOperation2").name));
	}
	
	public void testAssociateStyleWithOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertEquals(Operation.STYLE_DOCUMENT, (result.getDefinition("definitionNS").operations.get("testOperation1").style));
		assertEquals(Operation.STYLE_RPC, (result.getDefinition("definitionNS").operations.get("testOperation2").style));
	}
	
	public void testAssociateStyleWithOperationWhenDefaultTypeIsRPC() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations-default-style-rpc.wsdl")).parse();
		
		assertEquals(Operation.STYLE_RPC, (result.getDefinition("definitionNS").operations.get("testOperation1").style));
		assertEquals(Operation.STYLE_DOCUMENT, (result.getDefinition("definitionNS").operations.get("testOperation2").style));
	}
	
	public void testAssociateSoapActionWithOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertEquals("soapActionForTestOperation1", (result.getDefinition("definitionNS").operations.get("testOperation1").soapAction));
	}
	
	public void testAssociateNullSoapActionWithOperationWhenNoSoapActionForThisOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations-without-soap-action.wsdl")).parse();
		
		assertNull(result.getDefinition("definitionNS").operations.get("testOperation1").soapAction);
	}
	
	public void testAssociateInputWithOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertEquals(result.getDefinition("definitionNS").getMessage("messageOperation1"), 
				(result.getDefinition("definitionNS").operations.get("testOperation1").input));
	}
	
	public void testAssociateOutputWithOperation() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/with-operations.wsdl")).parse();
		
		assertEquals(result.getDefinition("definitionNS").getMessage("messageOperation1Output"), 
				(result.getDefinition("definitionNS").operations.get("testOperation1").output));
	}
	
	//TODO handle WSDL import and include (wsdl, not xsd)
	//TODO handle documentation so that it can be dumped by Client.explain()
}
