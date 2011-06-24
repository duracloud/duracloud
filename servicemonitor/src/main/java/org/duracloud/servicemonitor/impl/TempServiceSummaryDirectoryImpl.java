/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;

import java.io.InputStream;
import java.util.List;

/**
 * TO BE REMOVED UPON COMPLETION OF ServiceSummaryDirectoryImpl
 *
 * @author: Bill Branan
 * Date: 6/24/11
 */
public class TempServiceSummaryDirectoryImpl implements ServiceSummaryDirectory {

    @Override
    public List<ServiceSummary> getCurrentServiceSummaries() {
        // Default method body
        return null;
    }

    @Override
    public InputStream getCurrentServiceSummariesStream() {
        // Default method body
        return null;
    }

    @Override
    public List<ServiceSummary> getServiceSummariesById(String summaryId) {
        // Default method body
        return null;
    }

    @Override
    public InputStream getServiceSummariesStreamById(String summaryId) {
        // Default method body
        return null;
    }

    @Override
    public List<String> getServiceSummaryIds() {
        // Default method body
        return null;
    }

    @Override
    public void addServiceSummary(ServiceSummary serviceSummary) {
        // Default method body
    }
}
