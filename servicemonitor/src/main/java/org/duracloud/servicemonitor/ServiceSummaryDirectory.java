/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.error.ServiceSummaryNotFoundException;

import java.io.InputStream;
import java.util.List;

/**
 * This class manages the reading, writing, and listing of ServiceSummary
 * report content items.
 *
 * @author Andrew Woods
 *         Date: 6/24/11
 */
public interface ServiceSummaryDirectory {

    public static final String DATE_VAR = "$DATE";

    /**
     * This method initializes the ServiceSummaryDirectory.
     *
     * @param storeManager used by class
     */
    public void initialize(ContentStoreManager storeManager);

    /**
     * This method returns a list of ServiceSummaries for the current month.
     *
     * @return list of service summaries
     * @throws ServiceSummaryNotFoundException
     *          if no summary exists
     */
    public List<ServiceSummary> getCurrentServiceSummaries()
        throws ServiceSummaryNotFoundException;

    /**
     * This method returns a serialized list of ServiceSummaries for the
     * current month.
     *
     * @return serialized list of service summaries
     * @throws ServiceSummaryNotFoundException
     *          if no summary exists
     */
    public InputStream getCurrentServiceSummariesStream()
        throws ServiceSummaryNotFoundException;

    /**
     * This method returns a list of ServiceSummaries that correspond to the
     * arg id.
     *
     * @param summaryId of summary
     * @return list of service summaries
     * @throws ServiceSummaryNotFoundException
     *          if no summary exists
     */
    public List<ServiceSummary> getServiceSummariesById(String summaryId)
        throws ServiceSummaryNotFoundException;

    /**
     * This method returns a serialized list of ServiceSummaries that
     * correspond to the arg id.
     *
     * @param summaryId of summary
     * @return serialized list of service summaries
     * @throws ServiceSummaryNotFoundException
     *          if no summary exists
     */
    public InputStream getServiceSummariesStreamById(String summaryId)
        throws ServiceSummaryNotFoundException;

    /**
     * This method returns the list of summary ids.
     *
     * @return list of summary ids
     */
    public List<String> getServiceSummaryIds();

    /**
     * This method adds the arg service summary to the existing summary for
     * the month if one already exists, otherwise a new report is created with
     * the arg summary in it.
     *
     * @param serviceSummary to add to this month's report
     */
    public void addServiceSummary(ServiceSummary serviceSummary);

}
