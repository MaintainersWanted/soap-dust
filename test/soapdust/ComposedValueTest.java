package soapdust;

import java.util.List;

import junit.framework.TestCase;

public class ComposedValueTest extends TestCase {

	public void testAddAStringElement() {
		ComposedValue data = new ComposedValue().put("a", "1");
		assertEquals(new StringValue("1"), data.getValue2("a"));
	}

	public void testAddAComposedValue() {
		ComposedValue data = new ComposedValue().put("a", new ComposedValue().put("a", "1"));
		assertEquals(new ComposedValue().put("a", "1"), data.getValue2("a"));
	}
	
	public void testGettingEmptyComposedValueReturnsNull() {
		ComposedValue data = new ComposedValue().put("a", new ComposedValue());
		assertNull(data.getValue2("a"));
	}
	
	public void testAddTwoStringWithSameKeyGeneratesAListValue() {
		ComposedValue data = new ComposedValue().put("a", "1").put("a", "2");
		assertTrue(data.getValue2("a") instanceof ListValue);
	}
	
	public void testAddSeveralStringsWithSameKeyGeneratesAListValueWithAllTheStrings() {
		ComposedValue data = new ComposedValue()
		    .put("a", "1")
		    .put("a", "2")
		    .put("a", "3");

		List value = (List) data.getValue2("a").rawValue();
		assertEquals(3, value.size());
		assertEquals(new StringValue("1"), value.get(0));
		assertEquals(new StringValue("2"), value.get(1));
		assertEquals(new StringValue("3"), value.get(2));
	}
}
