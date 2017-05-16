package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.awt.image.BufferedImage;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.BaseEncoding;

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
	public void beginLine(int textSize) {
	}

	@Override
	public void endLine() {
		xmlTicket.writeElement("feed");
	}

	@Override
	public void printMessage(String message) {
		checkNotNull(message);
		checkReceiptOpen();
		xmlTicket.writeElement("text", message);
	}
	
	

	@Override
	public void printImage(BufferedImage image) {
		checkNotNull(image);
		
		// TODO Armar una rutina diferente para monocromo
		byte[] data = new byte[((image.getWidth()+1) /2) * image.getHeight()];
        for(int y=0;y<image.getHeight();y++){
            for(int x=0;x<image.getWidth();x++){
                int color=image.getRGB(x, y);
                int red   = (color >>> 16) & 0xFF;
                int green = (color >>>  8) & 0xFF;
                int blue  = (color >>>  0) & 0xFF;
                byte luminance = (byte) ((red * 0.2126f + green * 0.7152f + blue * 0.0722f)  / 16);
                
                
                data[x/2 + y* image.getHeight()] |= luminance << 4 * -(x % 2 - 1);
            }
        }
		xmlTicket.writeElement("image", BaseEncoding.base64().encode(data),
				ImmutableMap.of("mode","gray16",
				"width", String.valueOf(image.getWidth()),
				"height", String.valueOf(image.getHeight())
				));
	}

	private void checkReceiptOpen() {
		checkState(isOpen, "Must call beginReceipt() before sending commands.");
	}

}
