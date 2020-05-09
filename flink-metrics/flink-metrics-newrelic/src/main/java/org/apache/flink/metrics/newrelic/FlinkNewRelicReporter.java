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

import org.apache.flink.metrics.CharacterFilter;
import org.apache.flink.metrics.Counter;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.metrics.Meter;
import org.apache.flink.metrics.Metric;
import org.apache.flink.metrics.MetricConfig;
import org.apache.flink.metrics.MetricGroup;
import org.apache.flink.metrics.reporter.InstantiateViaFactory;
import org.apache.flink.metrics.reporter.MetricReporter;
import org.apache.flink.runtime.metrics.groups.AbstractMetricGroup;
import org.apache.flink.runtime.metrics.groups.FrontMetricGroup;
import org.apache.flink.util.NetUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link MetricReporter} that exports {@link Metric Metrics} to NewRelic.
 *
 * <p>Simple the wrapper for this to work in Flink
 * https://github.com/newrelic/dropwizard-metrics-newrelic
 */
@InstantiateViaFactory(factoryClassName = "org.apache.flink.metrics.newrelic.NewRelicReporterFactory")
public class FlinkNewRelicReporter extends ScheduledDropwizardReporter {
	protected String[] excluded_variables;
	@Override
	public ScheduledReporter getReporter(MetricConfig config) {
		excluded_variables = config.getString(ConfigConstants.METRICS_REPORTER_EXCLUDED_VARIABLES, "").split(";");

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
		for (String exclude: excluded_variables) {
			if (group.getMetricIdentifier(metricName).startsWith(exclude)) {
				toReport = false;
				break;
			}
		}
		if (toReport) super.notifyOfAddedMetric(metric, metricName, group);
	}
}
