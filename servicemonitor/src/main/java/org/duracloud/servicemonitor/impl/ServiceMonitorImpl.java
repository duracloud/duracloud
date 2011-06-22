/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.aop.DeployMessage;
import org.duracloud.servicemonitor.ServiceMonitor;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServiceMonitorImpl implements ServiceMonitor {

    private static final Logger log = LoggerFactory.getLogger(ServiceMonitorImpl.class);

    private static final String DATE_VAR = "$DATE";
    private static final long DEFAULT_MILLIS = 20000; // 20 seconds

    private String spaceId;
    private String contentId;

    private boolean continuePolling = true;
    private long pollingInterval;

    private ServiceSummaryWriter summaryWriter;
    private ServicesManager servicesManager;
    private ContentStore contentStore;


    public ServiceMonitorImpl(String spaceId, String contentId) {
        this(spaceId, contentId, DEFAULT_MILLIS, null, null, null);
    }

    public ServiceMonitorImpl(String spaceId,
                              String contentId,
                              long pollingInterval,
                              ServiceSummaryWriter summaryWriter,
                              ServicesManager servicesManager,
                              ContentStore contentStore) {
        log.info("Starting ServiceMonitor");

        this.spaceId = spaceId;
        this.contentId = filterContentId(contentId);
        this.pollingInterval = pollingInterval;
        this.summaryWriter = summaryWriter;
        this.servicesManager = servicesManager;
        this.contentStore = contentStore;
    }

    private String filterContentId(String contentId) {
        long nowLong = System.currentTimeMillis();
        String now = DateUtil.convertToStringYearMonth(nowLong);
        return contentId.replace(DATE_VAR, now);
    }


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


    private void startServicePoller(int serviceId, int deploymentId) {
        new Thread(new ServicePoller(serviceId,
                                     deploymentId,
                                     getSummaryWriter(),
                                     servicesManager,
                                     pollingInterval,
                                     continuePolling)).start();
    }

    private ServiceSummaryWriter getSummaryWriter() {
        if (null == summaryWriter) {
            summaryWriter = new ServiceSummaryWriterImpl(servicesManager,
                                                         contentStore,
                                                         spaceId,
                                                         contentId);
        }
        return summaryWriter;
    }

    @Override
    public void dispose() {
        continuePolling = false;
    }

    @Override
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public void setContentStore(ContentStore contentStore) {
        this.contentStore = contentStore;
    }

    private boolean isInitialized() {
        return null != servicesManager && null != contentStore;
    }

}
