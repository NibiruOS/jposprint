package ar.com.system.print.api;

import java.awt.image.BufferedImage;

public interface Printer {
	void beginReceipt();

	void endReceipt();

	void beginLine(int textSize);

	void endLine();

	void printMessage(String message);
	
	void printImage(BufferedImage image);
	
	void cut(String type);
}
