package soapdust;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * The main data structure used by soap-dust.
 * <p> 
 * A {@link ComposedValue} is a tree composed of other {@link ComposedValue} or of {@link String}.
 * <p>
 * For soap-dust, every data is either a {@link String} or a {@link ComposedValue}.
 * <p>
 * For instance if you need to send an int to a remote server, you
 * have to use its {@link String} representation. That is, for instance 200
 * for the int 200.
 */
public class ComposedValue extends Value {

	//TODO would'nt it be cool if this class implemented map and if element could be found with path string ?

	private HashMap<String, Value> children = new LinkedHashMap<String, Value>(); //ensure parameters are kept in addition order.
    public String type;

	public ComposedValue getComposedValue(String key) {
		return (ComposedValue) getValue(key, ComposedValue.class);
	}

	public String getStringValue(String key) {
		Value value = getValue(key, StringValue.class);
		return value == null ? null : value.toString();
	}

	public Value getValue(String key, Class<? extends Value> type) {

		Value value = children.get(key);

		if (value == null) {
			StringBuilder msg = new StringBuilder();
			msg.append("unknown key: ");
			msg.append(key);
			msg.append(" known keys are: ");
			for (String knownKey : children.keySet()) {
				msg.append(knownKey);
				msg.append(" ");
			}
			throw new IllegalArgumentException(msg.toString());
		}

		if (value.isNil()) return null;

		if (! type.isAssignableFrom(value.getClass())) {
			String expectedType = type.getSimpleName();
			String actualType = value.getClass().getSimpleName();
			throw new ClassCastException(key + " is not of type " + expectedType
					+ " but of type " + actualType + ". " 
					+ "Use get" + actualType + "(\"" + key + "\") instead.");
		}

		return value;
	}
	
	public Object getValue(String key) {
		//FIXME this method should return Value in the future
		Value value = getValue(key, Value.class);
		return value == null ? null : value.rawValue();
	}

	public Value getValue2(String key) {
		return getValue(key, Value.class);
	}

	public ComposedValue putValue(String key, Value value) {
		Value child = children.get(key);
		if(child != null) {
			value = new ListValue().append(child).append(value);
		}
		children.put(key, value);
		return this;
	}			

	/**
	 * Add an element to this {@link ComposedValue} with key key and value value.
	 * <p>
	 * If key refers to a list, call this method several times with the 
	 * same key and each value you want to add to the list.
	 * @return this {@link ComposedValue} to continue adding things to it.
	 */
	public ComposedValue put(String key, Object value) {
		if ((value instanceof String) 
				|| (value instanceof Value)) {
			if (value instanceof String) this.putValue(key, new StringValue((String) value));
			if (value instanceof Value) this.putValue(key, (Value) value);
		} else {
			String message = "While adding child \"" + key + "\": " 
			+ "Only String and ComposedValue are allowed in ComposedValue, not: " ;
			if (value == null) {
				throw new IllegalArgumentException(message + null);
			} else {
				throw new IllegalArgumentException(message + value.getClass());
			}
		}
		return this;
	}
	
	public Set<String> getChildrenKeys() {
		return children.keySet();
	}

	//---
	
	@Override
	public String toString() {
	    
	    StringBuilder result = new StringBuilder();
	    result.append("{");
	    Set<Entry<String,Value>> entrySet = children.entrySet();
	    String separator = "";
	    for (Entry<String, Value> entry : entrySet) {
	        result.append(separator);
            result.append("\"");
            result.append(entry.getKey().replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\""));
            result.append("\": \"");
            result.append(entry.getValue().toString());
            result.append("\"");
            separator = ", ";
        }
	    result.append("}");
	    
		return result.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ComposedValue) {
			ComposedValue other = (ComposedValue) obj;
			return this.children.equals(other.children);
		} else {
			return false;
		}
	}

//	@Override
//	public int hashCode() {
//		return this.children.hashCode();
//	}
	
	@Override
	public boolean isNil() {
		return children.isEmpty();
	}

	@Override
	public Object rawValue() {
		return this;
	}
}

class StringValue extends Value {
	String value;
	
	public StringValue(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringValue) {
			StringValue other = (StringValue) obj;
			return value.equals(other.value);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean isNil() {
		return false;
	}

	@Override
	public Object rawValue() {
		return value;
	}
}

class ListValue extends Value {

	List<Value> value = new ArrayList<Value>();
	
	@Override
	public boolean isNil() {
		return value.isEmpty();
	}

	public ListValue append(Value value) {
		if (value instanceof ListValue) {
			this.value.addAll(((ListValue) value).value);
		} else {
			this.value.add(value);
		}
		return this;
	}

	@Override
	public Object rawValue() {
		//FIXME directly implement List instead ?
		return value;
	}
	
	@Override
	public String toString() {
		return value.toString();
	}
}

abstract class Value {
	public abstract boolean isNil();
	public abstract Object rawValue();//FIXME transform code so that this method becomes useless
}
