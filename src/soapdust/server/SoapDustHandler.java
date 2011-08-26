package soapdust.server;

import soapdust.ComposedValue;

public interface SoapDustHandler {
	ComposedValue handle(String action, ComposedValue params);
}
