/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.DateUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.ServiceSummariesDocument;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryNotFoundException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import static org.duracloud.serviceconfig.xml.ServiceSummaryListDocumentBinding.createServiceSummaryListFrom;

/**
 * This class manages the reading, writing, and listing of ServiceSummary
 * report content items.
 *
 * @author Andrew Woods
 *         Date: 6/24/11
 */
public class ServiceSummaryDirectoryImpl implements ServiceSummaryDirectory {

    private static final Logger log = LoggerFactory.getLogger(
        ServiceSummaryDirectoryImpl.class);

    private ContentStore contentStore;
    private String spaceId;
    private String contentIdBase;


    public ServiceSummaryDirectoryImpl(String spaceId, String contentIdBase) {
        this(spaceId, contentIdBase, null);
    }

    public ServiceSummaryDirectoryImpl(String spaceId,
                                       String contentIdBase,
                                       ContentStore contentStore) {
        if (!contentIdBase.contains(DATE_VAR)) {
            StringBuilder error = new StringBuilder();
            error.append("contentIdBase passed into constructor must contain ");
            error.append("the variable: '" + DATE_VAR + "', found value: '");
            error.append(contentIdBase + "'");
            throw new DuraCloudRuntimeException(error.toString());
        }

        this.spaceId = spaceId;
        this.contentIdBase = contentIdBase;
        this.contentStore = contentStore;
    }

    @Override
    public void initialize(ContentStoreManager storeManager) {
        this.contentStore = getContentStore(storeManager);
    }

    private ContentStore getContentStore(ContentStoreManager storeManager) {
        try {
            return storeManager.getPrimaryContentStore();

        } catch (ContentStoreException e) {
            String error = "Error getting contentStore from storeManager!";
            log.error(error);
            throw new DuraCloudRuntimeException(error, e);
        }
    }

    private ContentStore getContentStore() {
        if (null == this.contentStore) {
            String error = "ServiceSummaryDirectory is not initialized!";
            log.error(error);
            throw new DuraCloudRuntimeException(error);
        }
        return contentStore;
    }

    @Override
    public List<ServiceSummary> getCurrentServiceSummaries()
        throws ServiceSummaryNotFoundException {
        InputStream stream = getCurrentServiceSummariesStream();
        List<ServiceSummary> summaries = createServiceSummaryListFrom(stream);
        IOUtils.closeQuietly(stream);

        return summaries;
    }

    @Override
    public InputStream getCurrentServiceSummariesStream()
        throws ServiceSummaryNotFoundException {
        return getContentItem(currentContentId());
    }

    @Override
    public List<ServiceSummary> getServiceSummariesById(String summaryId)
        throws ServiceSummaryNotFoundException {
        return ServiceSummariesDocument.getServiceSummaryList(getContentItem(
            summaryId));
    }

    @Override
    public InputStream getServiceSummariesStreamById(String summaryId)
        throws ServiceSummaryNotFoundException {
        return getContentItem(summaryId);
    }

    private InputStream getContentItem(String contentId)
        throws ServiceSummaryNotFoundException {
        Content content = null;
        try {
            content = getContentStore().getContent(spaceId, contentId);

        } catch (NotFoundException e) {
            StringBuilder warning = new StringBuilder();
            warning.append("Content item not found: ");
            warning.append(spaceId + " / " + contentId);
            log.warn(warning.toString());
            throw new ServiceSummaryNotFoundException(warning.toString(), e);

        } catch (ContentStoreException e) {
            StringBuilder error = new StringBuilder();
            error.append("Error getting content item: ");
            error.append(spaceId + " / " + contentId);
            log.error(error.toString());
            throw new DuraCloudRuntimeException(error.toString(), e);
        }

        InputStream stream = content.getStream();
        if (null == stream) {
            StringBuilder error = new StringBuilder();
            error.append("No stream found for item: ");
            error.append(spaceId + " / " + contentId);
            log.error(error.toString());
            throw new DuraCloudRuntimeException(error.toString());
        }
        return stream;
    }

    @Override
    public List<String> getServiceSummaryIds() {
        String prefix = contentIdBase.substring(0, contentIdBase.indexOf(
            DATE_VAR));

        Iterator<String> itr = null;
        try {
            itr = getContentStore().getSpaceContents(spaceId, prefix);

        } catch (ContentStoreException e) {
            StringBuilder error = new StringBuilder();
            error.append("Error getting space listing for, with prefix: ");
            error.append(spaceId + " / " + prefix);
            log.error(error.toString());
            throw new DuraCloudRuntimeException(error.toString(), e);
        }

        List<String> ids = new ArrayList<String>();
        while (itr.hasNext()) {
            ids.add(itr.next());
        }

        return ids;
    }

    @Override
    public void addServiceSummary(ServiceSummary serviceSummary) {
        String contentId = currentContentId();

        List<ServiceSummary> summaries;
        try {
            summaries = getCurrentServiceSummaries();

        } catch (ServiceSummaryNotFoundException e) {
            log.info("creating new service-summary listing: {} / {}",
                     spaceId,
                     contentId);
            summaries = new ArrayList<ServiceSummary>();
        }

        summaries.add(serviceSummary);
        String xml = ServiceSummariesDocument.getServiceSummaryListAsXML(
            summaries);

        try {
            InputStream stream = new AutoCloseInputStream(new ByteArrayInputStream(
                xml.getBytes()));
            getContentStore().addContent(spaceId,
                                         currentContentId(),
                                         stream,
                                         xml.length(),
                                         null,
                                         null,
                                         null);

        } catch (ContentStoreException e) {
            StringBuilder error = new StringBuilder();
            error.append("Error adding content item: ");
            error.append(spaceId + " / " + contentId);
            log.error(error.toString());
            throw new DuraCloudRuntimeException(error.toString(), e);
        }
    }

    private String currentContentId() {
        return filterContentId(Calendar.getInstance());
    }

    private String filterContentId(Calendar calendar) {
        String now = DateUtil.convertToStringYearMonth(calendar.getTimeInMillis());
        return contentIdBase.replace(DATE_VAR, now);
    }
}
