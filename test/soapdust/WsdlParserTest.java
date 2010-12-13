package soapdust;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

public class WsdlParserTest extends TestCase {

	public void testParseWsdlReturnsServiceDescriptionWithOperations() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		assertNotNull(result.operations.get("testOperation1"));
		assertNotNull(result.operations.get("testOperation2"));
	}
	
	public void testParseWsdlReturnsServiceDescriptionWithMessagePartsList() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		assertNotNull(result.messages.get("testOperation1"));
		assertNotNull(result.messages.get("messageParameter2"));
	}
	
	public void testParseWsdlAssociateSoapActionWithOperations() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		WsdlOperation operation = result.operations.get("testOperation1");
		assertEquals("soapActionForTestOperation1", operation.soapAction);
	}
	
	public void testParseWsdlAssociateSoapActionWithOperationStyleDocument() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		WsdlOperation operation = result.operations.get("testOperation1");
		assertEquals(WsdlOperation.DOCUMENT, operation.getStyle());
	}

	public void testParseWsdlAssociateSoapActionWithOperationStyleRpc() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/jira.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		WsdlOperation operation = result.operations.get("login");
		assertEquals(WsdlOperation.RPC, operation.getStyle());
	}

	public void testParseWsdlAssociateSoapActionWithOperationStyleOverridingSoapBinding() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		WsdlOperation operation = result.operations.get("testOperation2");
		assertEquals(WsdlOperation.RPC, operation.getStyle());
	}

	public void testParseWsdlAssociateNullSoapActionWithOperationsWithoutSoapActionInWsdl() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		WsdlOperation operation = result.operations.get("testOperation2");
		assertNull(operation.soapAction);
	}
	
	public void testParseWsdlAssociateMessagePartsWithOperation() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		
		ServiceDescription result = WsdlParser.parse(inputStream);
		
		WsdlOperation operation1 = result.operations.get("testOperation1");
		assertNotNull(operation1.parts.get("messageParameter1Element"));
		assertNotNull(operation1.parts.get("string"));
		WsdlOperation operation2 = result.operations.get("testOperation2");
		assertTrue(operation2.parts.isEmpty());
	}
	                                                               

	public void testParseWsdlAssociateMessagePartsWithNameSpace() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		
		assertEquals("definitionNS", result.messages.get("testOperation1").namespace);
		assertEquals("definitionNS", result.messages.get("messageParameter2").namespace);
	}
	
	
	public void testParseWsdlAssociateOperationsParametersWithNameSpaceRecursively() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		
		WsdlElement parameter1 = result.messages.get("messageParameter1Element");
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
	
	public void testParseWsdlAddAStarMessageAssociatedWithADefaultNamespace() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		
		assertEquals("definitionNS", result.messages.get("*").namespace);
	}

	public void testParseWsdlAddAStarOperationWithDefaultValues() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
		FileInputStream inputStream = new FileInputStream("test/soapdust/test.wsdl");
		ServiceDescription result = WsdlParser.parse(inputStream);
		
		WsdlOperation defaultOperation = result.operations.get("*");
		assertEquals("", defaultOperation.soapAction);
		assertEquals(WsdlOperation.RPC, defaultOperation.getStyle());
	}

}
