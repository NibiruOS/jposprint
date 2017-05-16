package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

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

import com.google.common.collect.ImmutableMap;

import ar.com.system.print.api.PrinterException;

public class XmlTicket {
	private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String SOAP_PREFIX = "s";
	private static final String EPOS_NS = "http://www.epson-pos.com/schemas/2011/03/epos-print";
	private static final String EPOS_PREFIX = "";

	private final XMLReader xmlReader;
	private final StringWriter xmlString;
	private final XMLStreamWriter xml;

	public XmlTicket() {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setNamespaceAware(true);
			xmlReader = spf.newSAXParser().getXMLReader();
			xmlString = new StringWriter();
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(xmlString);
		} catch (ParserConfigurationException | SAXException | XMLStreamException | FactoryConfigurationError e) {
			throw new PrinterException(e);
		}
	}

	void beginDocument() {
		try {
			xmlString.getBuffer().setLength(0);
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

	void endDocument() {
		try {
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndElement();
			xml.writeEndDocument();
			xml.flush();
		} catch (XMLStreamException e) {
			throw new PrinterException(e);
		}
	}

	void writeElement(String element) {
		writeElement(element, "");
	}

	void writeElement(String element, Map<String, String> attributes) {
		writeElement(element, "", attributes);
	}

	void writeElement(String element, String characters) {
		writeElement(element, characters, ImmutableMap.of());
	}

	void writeElement(String element, String characters, Map<String, String> attributes) {
		try {
			xml.writeStartElement(EPOS_PREFIX, element, EPOS_NS);
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				xml.writeAttribute(entry.getKey(), entry.getValue());
			}
			xml.writeCharacters(characters);
			xml.writeEndElement();
		} catch (XMLStreamException e) {
			throw new PrinterException(e);
		}
	}

	String getRequestXml() {
		return xmlString.toString();
	}

	Response parseResponseXml(Reader response) {
		try {
			ResponseHandler handler = new ResponseHandler();
			xmlReader.setContentHandler(handler);
			xmlReader.parse(new InputSource(response));
			return handler;
		} catch (IOException | SAXException e) {
			throw new PrinterException(e);
		}
	}

	interface Response {
		enum Status {
			ASB_NO_RESPONSE(0x00000001, "No response from TM printer"), 
			ASB_PRINT_SUCCESS(0x00000002, "Printing completed"), 
			ASB_DRAWER_KICK(0x00000004, "Drawer kick connector pin No.3 status = H / Offline status due to the battery level"), 
			ASB_OFF_LINE(0x00000008, "Offline status"), 
			ASB_COVER_OPEN(0x00000020, "Cover is open"), 
			ASB_PAPER_FEED(0x00000040, "Paper is being fed by the paper feed switch"), 
			ASB_WAIT_ON_LINE(0x00000100, "Waiting for recovery to online"), 
			ASB_PANEL_SWITCH(0x00000200, "Paper feed switch is held depressed"), 
			ASB_MECHANICAL_ERR(0x00000400, "Mechanical error occurred"), 
			ASB_AUTOCUTTER_ERR(0x00000800, "Auto cutter error occurred"), 
			ASB_UNRECOVER_ERR(0x00002000, "Unrecoverable error occurred"), 
			ASB_AUTORECOVER_ERR(0x00004000, "Automatic recovery error occurred"), 
			ASB_RECEIPT_NEAR_END(0x00020000, "Roll paper has almost run out"), 
			ASB_RECEIPT_END(0x00080000, "Roll paper has run out"), 
			ASB_BUZZER(0x01000000, "Buzzer is sounding"), 
			ASB_WAIT_REMOVE_LABEL(0x01000000, "Waiting for labels to be removed"), 
			ASB_NO_LABEL(0x04000000, "No paper is detected with the label peeler sensor"), 
			ASB_SPOOLER_IS_STOPPED(0x80000000, "Spooler stopped"), 
			UNKNOWN(-1, null);

			private static Status fromId(int id) {
				for (Status status : Status.values()) {
					if (status.id == id) {
						return status;
					}
				}
				return UNKNOWN;
			}

			private final int id;
			private final String message;

			private Status(int id, String message) {
				this.id = id;
				this.message = message;
			}

			public int getId() {
				return id;
			}

			public String getMessage() {
				return message;
			}
		}

		enum Code {
			
			EPTR_AUTOMATICAL("EPTR_AUTOMATICAL", "Automatic recovery error occurred"), 
			EPTR_BATTERY_LOW("EPTR_BATTERY_LOW", "Battery has run out"), 
			EPTR_COVER_OPEN("EPTR_COVER_OPEN", "Cover open error occurred"), 
			EPTR_CUTTER("EPTR_CUTTER", "Auto cutter error occurred"), 
			EPTR_MECHANICAL("EPTR_MECHANICAL", "Mechanical error occurred"), 
			EPTR_REC_EMPTY("EPTR_REC_EMPTY", "No paper is left in the roll paper end detector"), 
			EPTR_UNRECOVERABLE("EPTR_UNRECOVERABLE", "Unrecoverable error occurred"), 
			SCHEMA_ERROR("SchemaError", "Error exists in the requested document syntax"), 
			DEVICE_NOT_FOUND("DeviceNotFound", "Printer specified by the device ID does not exist"), 
			PRINT_SYSTEM_ERROR("PrintSystemError", "Error occurred with the printing system"), 
			EX_BADPORT("EX_BADPORT", "An error occurred with the communication port"), 
			EX_TIMEOUT("EX_TIMEOUT", "Print timeout occurred"), 
			EX_SPOOLER("EX_SPOOLER", "Print queue is full"), 
			JOB_NOT_FOUND("JobNotFound", "Specified job ID does not exist"), 
			PRINTING("Printing", "Printing in progress"), 
			UNKNOWN(null, null);

			private static Code fromId(String id) {
				checkNotNull(id);
				for (Code code : Code.values()) {
					if (id.equalsIgnoreCase(code.id)) {
						return code;
					}
				}
				return UNKNOWN;
			}

			private final String id;
			private final String message;

			private Code(String id, String message) {
				this.id = id;
				this.message = message;
			}

			public String getId() {
				return id;
			}

			public String getMessage() {
				return message;
			}
		}

		boolean isSuccess();

		String getCodeId();

		Code getCode();

		Status getStatus();

		int getStatusId();

		int getBatteryLevel();

		boolean isAcAdapterConnected();

		int getBattery();
	}

	private static class ResponseHandler extends DefaultHandler implements Response {
		private boolean success;
		private String code;
		private int status;
		private int battery;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			if (EPOS_NS.equals(uri) && "response".equals(localName)) {
				String successStr = attributes.getValue("success");
				String statusStr = attributes.getValue("status");
				String batteryStr = attributes.getValue("battery");
				checkNotNull(successStr, "Success attribute not found in the response");
				checkNotNull(statusStr, "Status attribute not found in the response");
				checkNotNull(batteryStr, "Battery attribute not found in the response");
				success = Boolean.TRUE.toString().equalsIgnoreCase(successStr) || "1".equals(successStr);
				code = attributes.getValue("code");
				status = Integer.parseInt(statusStr);
				battery = Integer.parseInt(batteryStr);
			}
		}

		@Override
		public boolean isSuccess() {
			return success;
		}

		@Override
		public String getCodeId() {
			return code;
		}

		@Override
		public Code getCode() {
			return Code.fromId(code);
		}

		@Override
		public Status getStatus() {
			return Status.fromId(status);
		}

		@Override
		public int getStatusId() {
			return status;
		}

		@Override
		public int getBatteryLevel() {
			return battery & 0xF;
		}

		@Override
		public boolean isAcAdapterConnected() {
			return (battery & 0xF00) == 0;
		}

		@Override
		public int getBattery() {
			return battery;
		}
	}
}
