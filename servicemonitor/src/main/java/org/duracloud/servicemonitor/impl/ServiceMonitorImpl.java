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
import org.duracloud.servicemonitor.ServiceMonitor;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServiceMonitorImpl implements ServiceMonitor {

    private static final Logger log = LoggerFactory.getLogger(ServiceMonitorImpl.class);

    private static final long DEFAULT_MILLIS = 20000; // 20 seconds

    private boolean continuePolling = true;
    private long pollingInterval;

    private ServiceSummaryDirectory summaryDirectory;
    private ServiceSummarizer summarizer = null;


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
    }

    @Override
    public void initialize(ServiceSummaryDirectory summaryDirectory,
                           ServiceSummarizer summarizer) {
        this.summaryDirectory = summaryDirectory;
        this.summarizer = summarizer;
    }

    @Override
    public void onDeploy(DeployMessage message) {
        log.info("ServiceMonitor.onDeploy({})", message);

        if (null == message) {
            String error = "Arg DeployMessage is null!";
            log.error(error);
            throw new IllegalArgumentException(error);
        }

        if (!isInitialized()) {
            String error = "ServiceManager and/or ContentStore uninitialized!";
            log.error(error);
            throw new DuraCloudRuntimeException(error);
        }

        int serviceId = message.getServiceId();
        int deploymentId = message.getDeploymentId();

        startServicePoller(serviceId, deploymentId);
    }

    private boolean isInitialized() {
        return null != summarizer && null != summaryDirectory;
    }

    private void startServicePoller(int serviceId, int deploymentId) {
        new Thread(new ServicePoller(serviceId,
                                     deploymentId,
                                     summaryDirectory,
                                     summarizer,
                                     pollingInterval,
                                     continuePolling)).start();
    }

    @Override
    public void dispose() {
        continuePolling = false;
    }

}
