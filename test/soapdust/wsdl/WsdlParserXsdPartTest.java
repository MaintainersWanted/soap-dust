package soapdust.wsdl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class WsdlParserXsdPartTest extends TestCase {
	
	public void testParseMalformedWsdlFails() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		try {
			new WsdlParser(new URL("file:test/soapdust/wsdl/malformed.wsdl")).parse();
			fail("SAXParserxception should be thrown for this invalid wsdl");
		} catch(SAXParseException e) {
			String msg = e.getMessage();
			assertTrue(msg, msg.contains("Invalid content was found starting with element 'type'"));
		}
	}

	public void testSchemaHasANameSpace() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-a-string.wsdl")).parse();
		
		assertEquals("element1NS", description.getSchema("element1NS").targetNameSpace);
		
	}

	public void testTypeAsAnElement() throws SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-a-string.wsdl")).parse();

		Type name = description.getSchema("element1NS").getType("name");
		assertType("element1NS", "name", name);
		assertEquals(0, name.getTypes().size());
	}
	
	public void testTypeAsAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-one-complex-type.wsdl")).parse();

		Type person = description.getSchema("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getType("firstname"));
		assertNotNull(person.getType("lastname"));
	}

	public void testTypeAsAnElementWhichIsAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-one-element-which-is-a-complex-type.wsdl")).parse();

		Type person = description.getSchema("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getType("firstname"));
		assertNotNull(person.getType("lastname"));
	}
	
	public void testTypeAsAnElementWhichIsOfAComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-one-element-which-is-of-a-complex-type.wsdl")).parse();

		Type person = description.getSchema("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getType("firstname"));
		assertNotNull(person.getType("lastname"));
	}
	
	public void testTypeAsAnElementWhichIsOfAComplexTypeWithoutNamespacePrefix() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/just-one-element-which-is-of-a-complex-type-without-ns-prefix.wsdl")).parse();

		Type person = description.getSchema("element1NS").getType("person");
		assertType("element1NS", "person", person);
		assertNotNull(person.getType("firstname"));
		assertNotNull(person.getType("lastname"));
	}
	
	public void testTypeAsASimpleTypeWithRestrictions() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/simple-type-with-restriction.wsdl")).parse();

		Type name = description.getSchema("element1NS").getType("SKU");
		assertType("element1NS", "SKU", name);
	}

	public void testComplexTypeComposedOfComplexType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/complex-type-of-complex-type.wsdl")).parse();
		//FIXME BIG ISSUE IN THE WAY IMPORTED TYPES ARE RETRIEVED : pb when targetnamesspace is remote but definition is later in the same file : tries to fetch a remote url :(
		
		Type car = description.getSchema("element2NS").getType("car");
		
		assertType("element2NS", "car", car);
		
		assertNotNull(car.getType("id"));
		
		assertType("element2NS", "owner", car.getType("owner"));
		assertType("element1NS", "firstname", car.getType("owner").getType("firstname"));
		assertType("element1NS", "lastname", car.getType("owner").getType("lastname"));
	}
	
	public void testDeepType() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/deep-type.wsdl")).parse();

		Type items = description.getSchema("element1NS").getType("Items");
		assertType("element1NS", "Items", items);
		Type item = items.getType("item");
		assertType("element1NS", "item", item);

		assertType("element1NS", "productName", item.getType("productName"));
		assertType("element1NS", "quantity", item.getType("quantity"));
		assertType("element1NS", "USPrice", item.getType("USPrice"));
		assertType("element1NS", "shipDate", item.getType("shipDate"));
	}
	
	public void testTouchyWsdl() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {
		WebServiceDescription description = 
			new WsdlParser(new URL("file:test/soapdust/wsdl/touchy.wsdl")).parse();

		Type purchaseOrder = description.getSchema("element1NS").getType("purchaseOrder");
		assertType("element1NS", "purchaseOrder", purchaseOrder);
		{
			Type shipTo = purchaseOrder.getType("shipTo");
			assertType("element1NS", "shipTo", shipTo);
			assertType("element1NS", "name", shipTo.getType("name"));
			assertType("element1NS", "street", shipTo.getType("street"));
			assertType("element1NS", "city", shipTo.getType("city"));
			assertType("element1NS", "state", shipTo.getType("state"));
			assertType("element1NS", "zip", shipTo.getType("zip"));
		}
		{
			Type billTo = purchaseOrder.getType("billTo");
			assertType("element1NS", "billTo", billTo);
			assertType("element1NS", "name", billTo.getType("name"));
			assertType("element1NS", "street", billTo.getType("street"));
			assertType("element1NS", "city", billTo.getType("city"));
			assertType("element1NS", "state", billTo.getType("state"));
			assertType("element1NS", "zip", billTo.getType("zip"));
		}
		{
			Type items = purchaseOrder.getType("items");
			assertType("element1NS", "items", items);
			{
				Type item = items.getType("item");
				assertType("element1NS", "item", item);
				assertType("element1NS", "productName", item.getType("productName"));
				assertType("element1NS", "quantity", item.getType("quantity"));
				assertType("element1NS", "USPrice", item.getType("USPrice"));
				assertType("element1NS", "shipDate", item.getType("shipDate"));
			}
		}
	}
	
	public void testMultiFileXsd() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {

		WebServiceDescription description = new WsdlParser(new URL("file:test/soapdust/wsdl/import-type.wsdl")).parse();

		Type car = description.getSchema("element2NS").getType("car");

		assertType("element2NS", "car", car);

		assertNotNull(car.getType("id"));

		Type owner = car.getType("owner");
		assertType("element2NS", "owner", owner);
		assertType("imported-type", "firstname", owner.getType("firstname"));
		assertType("imported-type", "lastname", owner.getType("lastname"));
	}
	
	public void testMultiFileXsdWithLocationInformation() throws FileNotFoundException, SAXException, IOException, ParserConfigurationException {

		WebServiceDescription description = new WsdlParser(new URL("file:test/soapdust/wsdl/import-type-with-location.wsdl")).parse();

		Type car = description.getSchema("element2NS").getType("car");

		assertType("element2NS", "car", car);

		assertNotNull(car.getType("id"));

		Type owner = car.getType("owner");
		assertType("element2NS", "owner", owner);
		assertType("imported-type-with-location", "firstname", owner.getType("firstname"));
		assertType("imported-type-with-location", "lastname", owner.getType("lastname"));
	}
	
	//TODO See first example from file:///home/vocal/Pascal-PERSO/XML%20Schema%20Part%200%3A%20Primer%20Second%20Edition.html
	//TODO parse really-touchy.wsdl

	//TODO 3.1 Target Namespaces & Unqualified Locals (xsd primer)

	//---
	
	private void assertType(String expectedNS, String expectedName, Type type) {
		assertNotNull(type);
		assertEquals(expectedNS, type.namespace);
		assertEquals(expectedName, type.name);
	}
}
