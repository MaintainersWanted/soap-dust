package soapdust;

import junit.framework.TestCase;

public class Base64EncoderTest extends TestCase {

    public void testEncodeString() {
        String userName = "a really secret ! Login@";
        String password = "an ^even more& secr3t passwd";
        
        String result = BASE64Encoder.encode((userName + ":" + password));
        
        assertEquals("YSByZWFsbHkgc2VjcmV0ICEgTG9naW5AOmFuIF5ldmVuIG1vcmUmIHNlY3IzdCBwYXNzd2Q=", result);
    }
}
