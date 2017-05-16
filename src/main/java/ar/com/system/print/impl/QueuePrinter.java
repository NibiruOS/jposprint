package ar.com.system.print.impl;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.awt.image.BufferedImage;
import java.util.Queue;

import com.google.common.collect.Queues;

import ar.com.system.print.api.Printer;
import ar.com.system.print.api.PrinterConnection;

public class QueuePrinter implements Printer {
	private final Printer printer;
	private final PrinterConnection connection;
	private boolean retail;
	private final Queue<Runnable> queue;

	public QueuePrinter(Printer printer,
			PrinterConnection connection,
			boolean retail) {
		this.printer = checkNotNull(printer);
		this.connection = checkNotNull(connection);
		this.retail = retail;
		this.queue = Queues.newArrayDeque();
	}

	public boolean isRetail() {
		return retail;
	}

	public void setRetail(boolean retail) {
		if (retail) {
			checkState(queue.isEmpty(), "Changing to retail while there are pending commands");
		}
		this.retail = retail;
	}

	@Override
	public void beginReceipt() {
		command(() -> printer.beginReceipt());
	}

	@Override
	public void endReceipt() {
		command(() -> printer.endReceipt());
		if (!retail) {
			connection.execute(() -> {
				while (!queue.isEmpty()) {
					queue.remove().run();
				}
			});
		}
	}

	@Override
	public void printMessage(String message) {
		checkNotNull(message);
		command(() -> printer.printMessage(message));
	}


	@Override
	public void beginLine(int textSize) {
		command(() -> printer.beginLine(textSize));
	}

	@Override
	public void endLine() {
		command(() -> printer.endLine());
	}

	@Override
	public void printImage(BufferedImage image) {
		checkNotNull(image);
		command(() -> printer.printImage(image));
	}

	private void command(Runnable command) {
		if (retail) {
			connection.execute(() -> {
				command.run();
			});
		} else {
			queue.add(command);
		}
	}

}
