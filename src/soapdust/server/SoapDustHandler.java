package soapdust.server;

import soapdust.ComposedValue;
import soapdust.FaultResponseException;

public interface SoapDustHandler {
	ComposedValue handle(String action, ComposedValue params) throws FaultResponseException;
}
