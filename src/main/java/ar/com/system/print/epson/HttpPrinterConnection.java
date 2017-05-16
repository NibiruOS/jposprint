package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Inject;

import ar.com.system.print.api.PrinterConnection;
import ar.com.system.print.api.PrinterException;
import ar.com.system.print.epson.XmlTicket.Response;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpPrinterConnection implements PrinterConnection {
	private final String url;
	private final OkHttpClient client;
	private final XmlTicket xmlTicket;
	private final BlockingQueue<Runnable> commands;

	@Inject
	public HttpPrinterConnection(String url, OkHttpClient client, XmlTicket xmlTicket) {
		this.url = checkNotNull(url);
		this.client = checkNotNull(client);
		this.xmlTicket = checkNotNull(xmlTicket);
		commands = new LinkedBlockingQueue<>();
		new Thread(() -> {
			while (true) {
				try {
					Runnable command = commands.take();
					xmlTicket.beginDocument();
					while (command != null) {
						command.run();
						command = commands.poll();
					}
					xmlTicket.endDocument();
					sendRequest(xmlTicket.getRequestXml());
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}).start();
	}

	@Override
	public void execute(Runnable callback) {
		try {
			commands.put(callback);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private void sendRequest(String requestXml) {
		try {

			System.out.println(requestXml);
			Request request = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), requestXml))
					.build();

			okhttp3.Response httpResponse = client.newCall(request).execute();

			Response response = xmlTicket.parseResponseXml(httpResponse.body().charStream());
			httpResponse.body().close();

			System.out.println("success " + response.isSuccess());
			System.out.println("status " + response.getStatus());
			System.out.println("code " + response.getCodeId());
			System.out.println("battery " + response.getBattery());
		} catch (IOException e) {
			throw new PrinterException(e);
		}
	}
}
