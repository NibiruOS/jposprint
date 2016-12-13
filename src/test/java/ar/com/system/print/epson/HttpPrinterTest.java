package ar.com.system.print.epson;

import org.junit.Before;
import org.junit.Test;

import ar.com.system.print.api.Printer;
import okhttp3.OkHttpClient;


public class HttpPrinterTest {
	private Printer printer;

	@Before
	public void setUp() throws Exception {
		printer = new HttpPrinter("http://192.4.1.13/cgi-bin/epos/service.cgi?devid=local_printer&timeout=60000", new OkHttpClient());
	}

	@Test
	public void printMessageTest() {
		printer.beginReceipt();
		printer.printMessage("Hello world");
		printer.endReceipt();
	}
}
