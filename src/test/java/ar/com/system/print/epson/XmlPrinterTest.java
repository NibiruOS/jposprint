package ar.com.system.print.epson;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;

import ar.com.system.print.DummySleeper;
import ar.com.system.print.impl.PrinterTest;

public class XmlPrinterTest extends PrinterTest {
	XmlTicket xmlTicket = new XmlTicket();

	@Before
	public void setUp() throws Exception {
		sleeper = new DummySleeper();
		printer = new XmlPrinter(xmlTicket);
		assertEquals("", xmlTicket.getRequestXml());
	}

	@After
	public void requestXmlTest() {
		assertEquals("<text>Hello world</text><feed></feed><text>Hello world</text><feed></feed><text>Hello world</text><feed></feed><text>Hello world</text><feed></feed><text>Hello world</text><feed></feed><text>Hello world</text><feed></feed><text>Hello world</text><feed></feed>", xmlTicket.getRequestXml());
	}
}
