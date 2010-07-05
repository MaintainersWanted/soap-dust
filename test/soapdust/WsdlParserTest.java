package soapdust;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class WsdlParserTest extends TestCase {

	public void testParseWsdlReturnsMessagePartsList() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		Map<String, WsdlElement> result = WsdlParser.parse(inputStream);
		assertNotNull(result.get("testOperation1"));
		assertNotNull(result.get("messageParameter2"));
	}
	
	public void testParseWsdlAssociateMessagePartsWithNameSpace() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		Map<String, WsdlElement> result = WsdlParser.parse(inputStream);
		
		assertEquals("definitionNS", result.get("testOperation1").namespace);
		assertEquals("definitionNS", result.get("messageParameter2").namespace);
	}

	public void testParseWsdlAssociateOperationsParametersWithNameSpaceRecursively() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		Map<String, WsdlElement> result = WsdlParser.parse(inputStream);
		
		WsdlElement parameter1 = result.get("testOperation1");
		assertEquals("element1NS", parameter1.children.get("sender").namespace);
		assertEquals("element1NS", parameter1.children.get("MSISDN").namespace);
		assertEquals("element1NS", parameter1.children.get("IDOffre").namespace);
		WsdlElement dosCli = parameter1.children.get("doscli");
		assertEquals("element1NS", dosCli.namespace);
		assertEquals("schema1NS", dosCli.children.get("subParameter1").namespace);
		assertEquals("schema1NS", dosCli.children.get("subParameter2").namespace);
		assertEquals("schema1NS", dosCli.children.get("subParameter3").namespace);
		WsdlElement subParameter4 = dosCli.children.get("subParameter4");
		assertEquals("schema1NS", subParameter4.namespace);
		assertEquals("element1NS", subParameter4.children.get("message").namespace);
		assertEquals("element1NS", subParameter4.children.get("untyped").namespace);
	}
	
	public void testParseWsdlAddAStarOperationAssociatedWithADefaultNamespace() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		Map<String, WsdlElement> result = WsdlParser.parse(inputStream);
		
		assertEquals("definitionNS", result.get("*").namespace);
	}
}
