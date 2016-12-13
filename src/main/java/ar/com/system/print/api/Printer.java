package ar.com.system.print.api;

public interface Printer {
	public void beginReceipt();

	public void endReceipt();

	public void printMessage(String message);
}
