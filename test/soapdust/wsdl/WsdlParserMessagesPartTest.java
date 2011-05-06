package soapdust.wsdl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class WsdlParserMessagesPartTest extends TestCase {
	
	public void testAssociateMessagesWithDefinition() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = new WsdlParser(new URL("file:test/soapdust/wsdl/with-simple-message.wsdl")).parse();
		
		assertNotNull(result.getDefinition("definitionNS").getMessage("messageOperation1"));
	}
	
	public void testAssociatesPartsWithMessages() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		WebServiceDescription result = new WsdlParser(new URL("file:test/soapdust/wsdl/with-simple-message.wsdl")).parse();
		
		Message message = result.getDefinition("definitionNS").getMessage("messageOperation1");
		assertNotNull(message.getPart("testPart1"));
	}
	
	public void testAssociatesXSDTypeWithAMessagePart() throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription result = new WsdlParser(new URL("file:test/soapdust/wsdl/with-simple-message.wsdl")).parse();
		
		Message message = result.getDefinition("definitionNS").getMessage("messageOperation1");
		assertEquals(result.findType("element1NS", "messageParameter1Element"), message.getPart("testPart1").type);
		assertEquals(result.findType("schema1NS", "messageSubParameter"), message.getPart("testPart2").type);
	}
	
	//TODO handle WSDL import and include (wsdl, not xsd)
	//TODO handle documentation so that it can be dumped by Client.explain()
}
