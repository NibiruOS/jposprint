package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import ar.com.system.print.api.Printer;
import ar.com.system.print.api.PrinterException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpPrinter implements Printer {
	private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String SOAP_PREFIX = "s";
	private static final String EPOS_NS = "http://www.epson-pos.com/schemas/2011/03/epos-print";
	private static final String EPOS_PREFIX = "";
	private final String url;
	private final OkHttpClient client;
	private final XMLReader xmlReader;
	private StringWriter xmlString;
	private XMLStreamWriter xml;

	@Inject
	public HttpPrinter(String url, OkHttpClient client) {
		this.url = checkNotNull(url);
		this.client = checkNotNull(client);
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			xmlReader = spf.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException | SAXException e) {
			throw new PrinterException(e);
		}
	}

	@Override
	public void beginReceipt() {
		try {
			xmlString = new StringWriter();
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(xmlString);
			xml.writeStartDocument();
			xml.writeStartElement(SOAP_PREFIX, "Envelope", SOAP_NS);
			xml.writeNamespace(SOAP_PREFIX, SOAP_NS);
			xml.writeStartElement(SOAP_PREFIX, "Body", SOAP_NS);
			xml.writeStartElement(EPOS_PREFIX, "epos-print", EPOS_NS);
			xml.writeNamespace(EPOS_PREFIX, EPOS_NS);
		} catch (XMLStreamException | FactoryConfigurationError e) {
			throw new PrinterException(e);
		}
	}

	@Override
	public void endReceipt() {
		try {
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndDocument();
			xml.flush();

			System.out.println(xmlString.toString());
			Request request = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), xmlString.toString())).build();

			Response response = client.newCall(request).execute();

			ResponseHandler handler = new ResponseHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(response.body().charStream()));
			response.body().close();
			
			System.out.println("success " + handler.isSuccess());
			System.out.println("status " + handler.getStatus());
			System.out.println("code " + handler.getCode());
			System.out.println("battery " + handler.getBattery());

			xmlString = null;
			xml = null;
		} catch (XMLStreamException | IOException | SAXException e) {
			throw new PrinterException(e);
		}
	}

	@Override
	public void printMessage(String message) {
		checkReceiptOpen();
		try {
			xml.writeStartElement(EPOS_PREFIX, "text", EPOS_NS);
			xml.writeCharacters(message);
			xml.writeEndElement();
			writeFeed();
		} catch (XMLStreamException e) {
			throw new PrinterException(e);
		}
	}

	private void checkReceiptOpen() {
		checkState(xml != null, "Must call beginReceipt() before sending commands.");
	}

	private void writeFeed() throws XMLStreamException {
		xml.writeStartElement(EPOS_PREFIX, "feed", EPOS_NS);
		xml.writeEndElement();
	}

	private static class ResponseHandler extends DefaultHandler {
		private boolean success;
		private String code;
		private int status;
		private int battery;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (EPOS_NS.equals(uri) && "response".equals(localName)) {
				success = Boolean.TRUE.toString().equalsIgnoreCase(attributes.getValue("success"));
				code = attributes.getValue("code");
				status = Integer.parseInt(attributes.getValue("status"));
				battery = Integer.parseInt(attributes.getValue("battery"));
			}
		}

		public boolean isSuccess() {
			return success;
		}

		public String getCode() {
			return code;
		}

		public int getStatus() {
			return status;
		}

		public int getBattery() {
			return battery;
		}
	}
}
