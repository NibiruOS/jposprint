package ar.com.system.print.impl;

import org.junit.Test;

import ar.com.system.print.Sleeper;
import ar.com.system.print.api.Printer;

public class PrinterTest {
	protected Sleeper sleeper;
	protected Printer printer;

	@Test
	public void printMessageTest() {
		printer.beginReceipt();
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		sleeper.sleep(3000);
		printer.printMessage("Hello world");
		printer.endReceipt();
	}
}
