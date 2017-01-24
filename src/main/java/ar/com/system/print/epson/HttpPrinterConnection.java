package ar.com.system.print.epson;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

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

	@Inject
	public HttpPrinterConnection(String url,
			OkHttpClient client,
			XmlTicket xmlTicket) {
		this.url = checkNotNull(url);
		this.client = checkNotNull(client);
		this.xmlTicket = checkNotNull(xmlTicket);
	}

	@Override
	public void execute(Runnable callback) {
		beginOperation();
		callback.run();
		finishOperation();
	}
	
	private void beginOperation() {
		xmlTicket.beginDocument();
	}

	private void finishOperation() {
		try {
			xmlTicket.endDocument();

			System.out.println(xmlTicket.getRequestXml());
			Request request = new Request.Builder().url(url)
					.post(RequestBody.create(MediaType.parse("text/xml; charset=utf-8"), xmlTicket.getRequestXml()))
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
