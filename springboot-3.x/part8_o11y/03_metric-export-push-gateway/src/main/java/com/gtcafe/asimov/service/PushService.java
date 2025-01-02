package com.gtcafe.asimov.service;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.PushGateway;
import io.prometheus.client.CollectorRegistry;

import java.io.IOException;

@Service
public class PushService {

    private final PrometheusMeterRegistry meterRegistry;

    public PushService(PrometheusMeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void pushToGateway(String jobName) throws IOException {
        PushGateway pushGateway = new PushGateway("localhost:9091"); // Pushgateway 地址
        CollectorRegistry registry = meterRegistry.getPrometheusRegistry();
        pushGateway.push(registry, jobName);
    }
}
