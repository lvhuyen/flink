/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.metrics.newrelic;

import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.InstantiateViaFactory;
import org.apache.flink.metrics.reporter.MetricReporter;

/**
 * {@link MetricReporter} that exports {@link Metric Metrics} to NewRelic.
 *
 * <p>Simple the wrapper for this to work in Flink
 * https://github.com/newrelic/dropwizard-metrics-newrelic
 */
@InstantiateViaFactory(factoryClassName = "org.apache.flink.metrics.newrelic.NewRelicReporterFactory")
public class FlinkNewRelicReporter extends ScheduledDropwizardReporter {
	protected String[] excludedVariables;
	@Override
	public ScheduledReporter getReporter(MetricConfig config) {
		excludedVariables = config.getString(ConfigConstants.METRICS_REPORTER_EXCLUDED_VARIABLES, "").split(";");

		String apiKey = config.getString("apiKey", null);
		String appName = config.getString("appName", null);

		MetricBatchSender metricBatchSender = SimpleMetricBatchSender
			.builder(apiKey, Duration.ofSeconds(5))
			.build();

		Attributes commonAttributes = new Attributes()
			.put("appName", appName);

		NewRelicReporter res = NewRelicReporter.build(registry, metricBatchSender)
			.commonAttributes(commonAttributes)
			.build();
		return res;
	}

	@Override
	public void notifyOfAddedMetric(Metric metric, String metricName, MetricGroup group) {
		boolean toReport = true;
		for (String exclude: excludedVariables) {
			if (group.getMetricIdentifier(metricName).startsWith(exclude)) {
				toReport = false;
				break;
			}
		}
		if (toReport) {
			super.notifyOfAddedMetric(metric, metricName, group);
		}
	}
}
