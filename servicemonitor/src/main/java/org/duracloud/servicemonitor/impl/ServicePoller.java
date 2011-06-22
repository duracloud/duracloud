/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.duracloud.services.ComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServicePoller implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServicePoller.class);

    private int serviceId;
    private int deploymentId;

    private ServiceSummaryWriter summaryWriter;
    private ServicesManager servicesManager;

    private long pollingInterval;
    private boolean continuePolling;

    public ServicePoller(int serviceId,
                         int deploymentId,
                         ServiceSummaryWriter summaryWriter,
                         ServicesManager servicesManager,
                         long pollingInterval,
                         boolean continuePolling) {
        this.serviceId = serviceId;
        this.deploymentId = deploymentId;
        this.summaryWriter = summaryWriter;
        this.servicesManager = servicesManager;
        this.pollingInterval = pollingInterval;
        this.continuePolling = continuePolling;
    }

    @Override
    public void run() {
        Map<String, String> props = getServiceProps();

        while (continuePolling && !serviceComplete(props)) {
            sleep(pollingInterval);
            props = getServiceProps();
        }

        if (serviceComplete(props)) {
            summaryWriter.collectAndWriteSummary(serviceId, deploymentId);
        }
    }

    private boolean serviceComplete(Map<String, String> props) {
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

    private Map<String, String> getServiceProps() {
        Map<String, String> props = new HashMap<String, String>();
        try {
            props = servicesManager.getDeployedServiceProps(serviceId,
                                                            deploymentId);
        } catch (Exception e) {
            log.warn(
                "Error getting service properties: serviceId={}, deploymentId={}, error={}",
                new Object[]{serviceId, deploymentId, e.getMessage()});
        }
        return props;
    }

    private void sleep(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

}
