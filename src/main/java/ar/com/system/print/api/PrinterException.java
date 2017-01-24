package ar.com.system.print.api;

public class PrinterException extends RuntimeException {
	public PrinterException(String root) {
		super(root);
	}

	public PrinterException(Throwable root) {
		super(root);
	}
}
