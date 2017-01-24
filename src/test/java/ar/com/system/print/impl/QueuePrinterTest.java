package ar.com.system.print.impl;

import org.junit.Before;

import ar.com.system.print.ThreadSleeper;
import ar.com.system.print.epson.HttpPrinterConnection;
import ar.com.system.print.epson.XmlPrinter;
import ar.com.system.print.epson.XmlTicket;
import okhttp3.OkHttpClient;

public class QueuePrinterTest extends PrinterTest {
	@Before
	public void setUp() throws Exception {
		sleeper = new ThreadSleeper();
		XmlTicket xmlTicket = new XmlTicket();
		printer = new QueuePrinter(
				new XmlPrinter(xmlTicket),
				new HttpPrinterConnection("http://192.4.1.73/cgi-bin/epos/service.cgi?devid=local_printer&timeout=60000",
						new OkHttpClient(),
						xmlTicket),
				false);
	}
}
