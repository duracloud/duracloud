/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.common.model.AclType;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class wraps a StorageProvider implementation, collecting timing metrics
 * while passing calls down.
 *
 * @author Andrew Woods
 */
public abstract class ProbedStorageProvider
        implements StorageProvider, MetricsProbed {

    protected StorageProvider storageProvider;

    protected MetricsTable metricsTable;

    protected Metric metric;

    abstract protected MetricsProbed getProbedCore();

    protected void startMetric(String methodName) {
        if (metric == null) {
            metric = new Metric(getClass().getName(), methodName);
            getMetricsTable().addMetric(metric);
        } else {
            metric.addElement(methodName);
        }

        MetricsTable subTable = new MetricsTable();
        this.metricsTable.addSubMetric(metric, subTable);
        MetricsProbed probed = getProbedCore();
        probed.setMetricsTable(subTable);

        metric.start(methodName);
    }

    protected void stopMetric(String methodName) {
        metric.stop(methodName);
    }

    public void setMetricsTable(MetricsTable metricsTable) {
        this.metricsTable = metricsTable;
        this.metric = null;
    }

    private MetricsTable getMetricsTable() {
        if (this.metricsTable == null) {
            throw new RuntimeException(new MetricException("Metrics table has not been set."));
        }
        return this.metricsTable;
    }

    protected void setStorageProvider(StorageProvider storageProvider) {
        this.storageProvider = storageProvider;
    }

    @Override
    public StorageProviderType getStorageProviderType() {
        startMetric("getStorageProviderType");
        StorageProviderType type = storageProvider.getStorageProviderType();
        stopMetric("getStorageProviderType");
        return type;
    }

    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) throws StorageException {
        startMetric("addContent");
        String result =
                storageProvider.addContent(spaceId,
                                           contentId,
                                           contentMimeType,
                                           userProperties,
                                           contentSize,
                                           contentChecksum,
                                           content);
        stopMetric("addContent");
        return result;
    }

    @Override
    public String copyContent(String sourceSpaceId,
                              String sourceContentId,
                              String destSpaceId,
                              String destContentId) {
        startMetric("copyContent");
        String result = storageProvider.copyContent(sourceSpaceId,
                                                    sourceContentId,
                                                    destSpaceId,
                                                    destContentId);
        stopMetric("copyContent");
        return result;
    }

    public void createSpace(String spaceId) throws StorageException {
        startMetric("createSpace");
        storageProvider.createSpace(spaceId);
        stopMetric("createSpace");
    }

    public void deleteContent(String spaceId, String contentId)
            throws StorageException {
        startMetric("deleteContent");
        storageProvider.deleteContent(spaceId, contentId);
        stopMetric("deleteContent");
    }

    public void deleteSpace(String spaceId) throws StorageException {
        startMetric("deleteSpace");
        storageProvider.deleteSpace(spaceId);
        stopMetric("deleteSpace");
    }

    public InputStream getContent(String spaceId, String contentId)
            throws StorageException {
        startMetric("getContent");
        InputStream result = storageProvider.getContent(spaceId, contentId);
        stopMetric("getContent");
        return result;
    }

    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId)
            throws StorageException {
        startMetric("getContentProperties");
        Map<String, String> result =
                storageProvider.getContentProperties(spaceId, contentId);
        stopMetric("getContentProperties");
        return result;
    }

    public Iterator<String> getSpaceContents(String spaceId, String prefix)
            throws StorageException {
        startMetric("getSpaceContents");
        Iterator<String> result =
            storageProvider.getSpaceContents(spaceId, prefix);
        stopMetric("getSpaceContents");
        return result;
    }

    public List<String> getSpaceContentsChunked(String spaceId,
                                                String prefix,
                                                long maxResults,
                                                String marker)
        throws StorageException {
        startMetric("getSpaceContents");
        List<String> result = storageProvider.getSpaceContentsChunked(spaceId,
                                                                      prefix,
                                                                      maxResults,
                                                                      marker);
        stopMetric("getSpaceContents");
        return result;
    }

    public Map<String, String> getSpaceProperties(String spaceId)
            throws StorageException {
        startMetric("getSpaceProperties");
        Map<String, String> result = storageProvider.getSpaceProperties(spaceId);
        stopMetric("getSpaceProperties");
        return result;
    }

    public Iterator<String> getSpaces() throws StorageException {
        startMetric("getSpaces");
        Iterator<String> result = storageProvider.getSpaces();
        stopMetric("getSpaces");
        return result;
    }

    public void setContentProperties(String spaceId,
                                     String contentId,
                                     Map<String, String> contentProperties)
            throws StorageException {
        startMetric("setContentProperties");
        storageProvider.setContentProperties(spaceId,
                                             contentId,
                                             contentProperties);
        stopMetric("setContentProperties");
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        startMetric("getSpaceACLs");
        Map<String, AclType> result = storageProvider.getSpaceACLs(spaceId);
        stopMetric("getSpaceACLs");
        return result;
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        startMetric("setSpaceACLs");
        storageProvider.setSpaceACLs(spaceId, spaceACLs);
        stopMetric("setSpaceACLs");
    }

}
