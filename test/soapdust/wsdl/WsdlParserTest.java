package soapdust.wsdl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import junit.framework.TestCase;

public class WsdlParserTest extends TestCase {
	
	public void testParseMalformedWsdlFails() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		try {
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/malformed.wsdl"));
			fail("SAXParserxception should be thrown for this invalid wsdl");
		} catch(SAXParseException e) {
			String msg = e.getMessage();
			assertTrue(msg, msg.contains("Invalid content was found starting with element 'type'"));
		}
	}

	public void testSchemaHasANameSpace() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-a-string.wsdl"));
		
		assertEquals("element1NS", description.schemas.get("element1NS").targetNameSpace);
		
	}

	public void testTypeAsAnElement() throws SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-a-string.wsdl"));

		Type name = description.schemas.get("element1NS").getType("name");
		assertType("element1NS", "name", name);
		assertEquals(0, name.getElements().size());
	}
	
	public void testTypeAsAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-one-complex-type.wsdl"));

		Type person = description.schemas.get("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getElement("firstname"));
		assertNotNull(person.getElement("lastname"));
	}

	public void testTypeAsAnElementWhichIsAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-one-element-which-is-a-complex-type.wsdl"));

		Type person = description.schemas.get("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getElement("firstname"));
		assertNotNull(person.getElement("lastname"));
	}
	
	public void testTypeAsAnElementWhichIsOfAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-one-element-which-is-of-a-complex-type.wsdl"));

		Type person = description.schemas.get("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getElement("firstname"));
		assertNotNull(person.getElement("lastname"));
	}
	
	public void testTypeAsAnElementWhichIsOfAComplexTypeWithoutNamespacePrefix() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/just-one-element-which-is-of-a-complex-type-without-ns-prefix.wsdl"));

		Type person = description.schemas.get("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getElement("firstname"));
		assertNotNull(person.getElement("lastname"));
	}
	
	public void testTypeAsASimpleTypeWithRestrictions() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/simple-type-with-restriction.wsdl"));

		Type name = description.schemas.get("element1NS").getType("SKU");
		assertType("element1NS", "SKU", name);
	}

	public void testComplexTypeComposedOfComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser().parse(new FileInputStream("test/soapdust/wsdl/complex-type-of-complex-type.wsdl"));

		Type car = description.schemas.get("element2NS").getType("car");
		
		assertType("element2NS", "car", car);
		
		assertNotNull(car.getElement("id"));
		
		assertType("element2NS", "owner", car.getElement("owner"));
		assertType("element1NS", "firstname", car.getElement("owner").getElement("firstname"));
		assertType("element1NS", "lastname", car.getElement("owner").getElement("lastname"));
	}
	
	//TODO See first example from file:///home/vocal/Pascal-PERSO/XML%20Schema%20Part%200%3A%20Primer%20Second%20Edition.html
	//TODO parse touchy.wsdl

	//TODO 3.1 Target Namespaces & Unqualified Locals (xsd primer)

	//---
	
	private void assertType(String expectedNS, String expectedName, Type name) {
		assertEquals(expectedNS, name.nameSpace);
		assertEquals(expectedName, name.name);
	}
}
