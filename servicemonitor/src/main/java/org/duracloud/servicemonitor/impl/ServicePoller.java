/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryException;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class polls deployed services waiting for them to complete.
 * Once a service is complete, this class initiates the collection and writing
 * of the service summary.
 *
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServicePoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServicePoller.class);

    private int serviceId;
    private int deploymentId;

    private ServiceSummaryDirectory summaryDirectory;
    private ServiceSummarizer summarizer;

    private long pollingInterval;
    private boolean continuePolling;

    public ServicePoller(int serviceId,
                         int deploymentId,
                         ServiceSummaryDirectory summaryDirectory,
                         ServiceSummarizer summarizer,
                         long pollingInterval) {
        this.serviceId = serviceId;
        this.deploymentId = deploymentId;
        this.summaryDirectory = summaryDirectory;
        this.summarizer = summarizer;
        this.pollingInterval = pollingInterval;
        this.continuePolling = true;
    }

    @Override
    public void run() {

        while (continuePolling && !isServiceComplete()) {
            sleep(pollingInterval);
        }

        if (continuePolling) { // means the service is complete
            ServiceSummary summary = getServiceSummary();
            if (null != summary) {
                summaryDirectory.addServiceSummary(summary);
            }
        }
    }

    private boolean isServiceComplete() {
        Map<String, String> props = summarizer.getServiceProps(serviceId,
                                                               deploymentId);
        for (String key : props.keySet()) {
            ComputeService.ServiceStatus status = propAsStatus(props.get(key));
            if (status.isComplete()) {
                return true;
            }
        }
        return false;
    }

    private ComputeService.ServiceStatus propAsStatus(String prop) {
        try {
            return ComputeService.ServiceStatus.valueOf(prop);

        } catch (Exception e) {
            return ComputeService.ServiceStatus.UNKNOWN;
        }
    }

    private ServiceSummary getServiceSummary() {
        try {
            return summarizer.summarizeService(serviceId, deploymentId);

        } catch (ServiceSummaryException e) {
            StringBuilder error = new StringBuilder();
            error.append("Error: Service complete, but unable to get summary!");
            error.append(" serviceId = " + serviceId);
            error.append(" deploymentId = " + deploymentId);
            error.append(" message: " + e.getMessage());
            log.error(error.toString());
            return null;
        }
    }

    private void sleep(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    public void stop() {
        log.info("Stopping ServicePoller: {}-{}", serviceId, deploymentId);
        continuePolling = false;
    }

}
