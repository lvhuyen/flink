package org.apache.flink.metrics.newrelic;

import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;

/**
 * Love it or leave it.
 */
public class TempHttpPusher extends OkHttpPoster {
	Logger logger = null;
	public TempHttpPusher(Duration callTimeout, Logger logger) {
		super(callTimeout);
		logger.info("Creating TempHttpPoster");
		this.logger = logger;
	}

	/** Create an OkHttpPoster with your own OkHttpClient implementation. */
	public TempHttpPusher(OkHttpClient client) {
		super(client);
	}

	@Override
	public HttpResponse post(URL url, Map<String, String> headers, byte[] body, String mediaType)
		throws IOException {
		logger.info("Pushing metrics to NewRelic " + url.toString() + headers.toString());
		throw new IOException("HTTPPost has been called");
	}

}
