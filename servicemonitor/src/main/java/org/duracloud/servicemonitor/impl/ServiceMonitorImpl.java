/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.serviceapi.aop.DeployMessage;
import org.duracloud.serviceapi.aop.ServiceMessage;
import org.duracloud.servicemonitor.ServiceMonitor;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServiceMonitorImpl implements ServiceMonitor {

    private static final Logger log = LoggerFactory.getLogger(ServiceMonitorImpl.class);

    private static final long DEFAULT_MILLIS = 20000; // 20 seconds

    private long pollingInterval;

    private ServiceSummaryDirectory summaryDirectory;
    private ServiceSummarizer summarizer = null;

    private Map<String, ServicePoller> pollers;

    public ServiceMonitorImpl() {
        this(DEFAULT_MILLIS, null, null);
    }

    public ServiceMonitorImpl(long pollingInterval,
                              ServiceSummaryDirectory summaryDirectory,
                              ServiceSummarizer summarizer) {
        log.info("Starting ServiceMonitor");

        this.pollingInterval = pollingInterval;
        this.summaryDirectory = summaryDirectory;
        this.summarizer = summarizer;
        this.pollers = new HashMap<String, ServicePoller>();
    }

    @Override
    public void initialize(ServiceSummaryDirectory summaryDirectory,
                           ServiceSummarizer summarizer) {
        this.summaryDirectory = summaryDirectory;
        this.summarizer = summarizer;

        if (!pollers.isEmpty()) {
            dispose();
        }
    }

    @Override
    public void onDeploy(DeployMessage message) {
        log.info("ServiceMonitor.onDeploy({})", message);
        checkInitialized();

        if (null == message) {
            String error = "Arg DeployMessage is null!";
            log.error(error);
            throw new IllegalArgumentException(error);
        }

        int serviceId = message.getServiceId();
        int deploymentId = message.getDeploymentId();

        startServicePoller(serviceId, deploymentId);
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            String error = "ServiceManager and/or ContentStore uninitialized!";
            log.error(error);
            throw new DuraCloudRuntimeException(error);
        }
    }

    private boolean isInitialized() {
        return null != summarizer && null != summaryDirectory;
    }

    private void startServicePoller(int serviceId, int deploymentId) {
        ServicePoller poller = new ServicePoller(serviceId,
                                                 deploymentId,
                                                 summaryDirectory,
                                                 summarizer,
                                                 pollingInterval);
        pollers.put(pollerId(serviceId, deploymentId), poller);

        new Thread(poller).start();
    }

    public void onUndeploy(ServiceMessage message) {
        log.info("ServiceMonitor.onUndeploy({})", message);
        checkInitialized();

        if (null == message) {
            String error = "Arg UnDeployMessage is null!";
            log.error(error);
            throw new IllegalArgumentException(error);
        }

        int serviceId = message.getServiceId();
        int deploymentId = message.getDeploymentId();

        String pollerId = pollerId(serviceId, deploymentId);
        ServicePoller poller = pollers.remove(pollerId);
        if (null != poller) {
            poller.stop();

        } else {
            log.warn("Poller not found in monitor map: {}", pollerId);
        }
    }

    private String pollerId(int serviceId, int deploymentId) {
        return serviceId + "-" + deploymentId;
    }

    @Override
    public void dispose() {
        for (String key : pollers.keySet()) {
            ServicePoller poller = pollers.remove(key);
            poller.stop();
        }
    }

}
