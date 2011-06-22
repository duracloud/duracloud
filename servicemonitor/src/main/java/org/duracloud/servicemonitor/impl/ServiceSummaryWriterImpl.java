/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public class ServiceSummaryWriterImpl implements ServiceSummaryWriter {

    private static final Logger log = LoggerFactory.getLogger(
        ServiceSummaryWriterImpl.class);

    private ServicesManager servicesManager;
    private ContentStore contentStore;
    private String spaceId;
    private String contentId;

    public ServiceSummaryWriterImpl(ServicesManager servicesManager,
                                    ContentStore contentStore,
                                    String spaceId,
                                    String contentId) {
        this.servicesManager = servicesManager;
        this.contentStore = contentStore;
        this.spaceId = spaceId;
        this.contentId = contentId;
    }

    public void collectAndWriteSummary(int serviceId, int deploymentId) {
        String summaryXml = collectSummary(serviceId, deploymentId);
        writeSummary(spaceId, contentId, summaryXml);
    }

    private String collectSummary(int serviceId, int deploymentId) {
        ServiceInfo serviceInfo = getServiceConfig(serviceId, deploymentId);
        Map<String, String> props = getServiceProps(serviceId, deploymentId);

        return serviceSummary(serviceInfo, props);
    }

    private ServiceInfo getServiceConfig(int serviceId, int deploymentId) {
        try {
            return servicesManager.getDeployedService(serviceId, deploymentId);

        } catch (NotFoundException e) {
            log.warn(
                "getServiceConfig, not found: serviceId={}, deploymentId={}, msg={}",
                new Object[]{serviceId, deploymentId, e.getMessage()});

        } catch (ServicesException e) {
            log.warn(
                "getServiceConfig, error: serviceId={}, deploymentId={}, msg={}",
                new Object[]{serviceId, deploymentId, e.getMessage()});

        }
        return new ServiceInfo();
    }

    private Map<String, String> getServiceProps(int serviceId,
                                                int deploymentId) {
        try {
            return servicesManager.getDeployedServiceProps(serviceId,
                                                           deploymentId);

        } catch (NotFoundException e) {
            log.warn(
                "getServiceProps, not found: serviceId={}, deploymentId={}, msg={}",
                new Object[]{serviceId, deploymentId, e.getMessage()});

        } catch (ServicesException e) {
            log.warn(
                "getServiceProps, error: serviceId={}, deploymentId={}, msg={}",
                new Object[]{serviceId, deploymentId, e.getMessage()});
        }

        return new HashMap<String, String>();
    }

    private String serviceSummary(ServiceInfo serviceInfo,
                                  Map<String, String> props) {
        // TODO: create xml document
        return "hello";
    }

    private void writeSummary(String spaceId,
                              String contentId,
                              String summaryXml) {
        InputStream xml = new AutoCloseInputStream(new ByteArrayInputStream(
            summaryXml.getBytes()));

        try {
            contentStore.addContent(spaceId,
                                    contentId,
                                    xml,
                                    summaryXml.length(),
                                    null,
                                    null,
                                    null);

        } catch (ContentStoreException e) {
            log.error("writeSummary, error: spaceId={}, contentId={}, msg={}",
                      new Object[]{spaceId, contentId, e.getMessage()});
        }
    }
}
