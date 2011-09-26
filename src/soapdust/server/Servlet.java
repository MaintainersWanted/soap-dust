package soapdust.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import soapdust.ComposedValue;
import soapdust.FaultResponseException;
import soapdust.MalformedResponseException;
import soapdust.SoapMessageParser;
import soapdust.wsdl.WebServiceDescription;
import soapdust.wsdl.WsdlParser;

public class Servlet extends HttpServlet {
	private Map<String, SoapDustHandler> handlers = new HashMap<String, SoapDustHandler>();
	private WebServiceDescription serviceDescription;

	public Servlet(String wsdlUrl) throws MalformedURLException, SAXException, IOException, ParserConfigurationException {
		serviceDescription = new WsdlParser(new URL(wsdlUrl)).parse();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String action = req.getHeader("SOAPAction");
		SoapDustHandler handler = handlers.get(action);
		handler = handler == null ? new DefaultHandler() : handler;
		ComposedValue params;
		try {
			params = new SoapMessageParser().parse(req.getInputStream());
		} catch (MalformedResponseException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException(e);
		}
		Document soapResponse;
		try {
			soapResponse = newDocument();
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
		Element body = createSoapBody(soapResponse);
		try {
			handler.handle(action, params);
		} catch (FaultResponseException e) {
			resp.setStatus(500);

			Element fault = soapResponse.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Fault");
			Element faultCode = soapResponse.createElement("faultcode");
			Element faultString = soapResponse.createElement("faultstring");
			body.appendChild(fault);
			fault.appendChild(faultCode);
			fault.appendChild(faultString);
			faultCode.appendChild(soapResponse.createTextNode(e.fault.getStringValue("faultcode")));
			faultString.appendChild(soapResponse.createTextNode(e.fault.getStringValue("faultstring")));

		}
		try {
			sendSoapResponse(resp, soapResponse);
		} catch(TransformerException e2) {
			throw new RuntimeException("Unexpected exception while sending soap response to client: " + e2, e2);
		}
	}

	private void sendSoapResponse(HttpServletResponse resp, Document document)
			throws IOException, TransformerConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		OutputStream out = resp.getOutputStream();
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(document), new StreamResult(out));
			out.flush();
		} finally {
			out.close();
		}
	}

	private Document newDocument() throws ParserConfigurationException {
		Document document;
		DocumentBuilder documentBuilder;
		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		document = documentBuilder.newDocument();
		return document;
	}

	public Servlet register(String operation, SoapDustHandler handler) {
		handlers.put(operation, handler);
		return this;
	}

	//---
	
	private Element createSoapBody(Document document) {
		Element envelope = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Envelope");
		Element header = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Header");
		Element body = document.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
		
		document.appendChild(envelope);
		envelope.appendChild(header);
		envelope.appendChild(body);
		return body;
	}
}

class DefaultHandler implements SoapDustHandler {
	@Override
	public ComposedValue handle(String action, ComposedValue params) throws FaultResponseException {
		throw new FaultResponseException(new ComposedValue()
		.put("faultcode", "UnsupportedOperation")
		.put("faultstring", "Unsupported operation: " + action));
	}
}
