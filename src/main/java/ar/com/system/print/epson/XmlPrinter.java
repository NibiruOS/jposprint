package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.inject.Inject;

import ar.com.system.print.api.Printer;

public class XmlPrinter implements Printer {
	private final XmlTicket xmlTicket;
	private boolean isOpen;

	@Inject
	public XmlPrinter(XmlTicket xmlTicket) {
		this.xmlTicket = checkNotNull(xmlTicket);
	}

	@Override
	public void beginReceipt() {
		checkState(!isOpen, "Ticket already open.");
		isOpen = true;
	}

	@Override
	public void endReceipt() {
		checkReceiptOpen();
		isOpen = false;
	}

	@Override
	public void printMessage(String message) {
		checkReceiptOpen();
		xmlTicket.writeElement("text", message);
		writeFeed();
	}

	private void writeFeed() {
		xmlTicket.writeElement("feed");
	}

	private void checkReceiptOpen() {
		checkState(isOpen, "Must call beginReceipt() before sending commands.");
	}
}
