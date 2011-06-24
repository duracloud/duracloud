/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor;

import org.duracloud.serviceconfig.ServiceSummary;

import java.io.InputStream;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 6/24/11
 */
public interface ServiceSummaryDirectory {

    public List<ServiceSummary> getCurrentServiceSummaries();
    public InputStream getCurrentServiceSummariesStream();

    public List<ServiceSummary> getServiceSummariesById(String summaryId);
    public InputStream getServiceSummariesStreamById(String summaryId);

    public List<String> getServiceSummaryIds();

    public void addServiceSummary(ServiceSummary serviceSummary);

}
