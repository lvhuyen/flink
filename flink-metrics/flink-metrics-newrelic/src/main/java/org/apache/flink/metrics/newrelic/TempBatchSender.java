package org.apache.flink.metrics.newrelic;

import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSenderBuilder;
import org.slf4j.Logger;

import java.time.Duration;

/**
 * Love it or leave it.
 */
public class TempBatchSender extends SimpleMetricBatchSender {
	public static MetricBatchSenderBuilder builder(String apiKey, Duration callTimeout, Logger logger) {
		OkHttpPoster okHttpPoster = new TempHttpPusher(callTimeout, logger);
		return MetricBatchSender.builder().apiKey(apiKey).httpPoster(okHttpPoster);
	}
}
