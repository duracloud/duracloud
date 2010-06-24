/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.auth.CredentialsProvider;

import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

/**
 * This class wraps a RestS3Service implementation, collecting timing metrics
 * while passing calls down.
 *
 * @author Andrew Woods
 */
public class ProbedRestS3Service
        extends RestS3Service
        implements MetricsProbed {

    private static final long serialVersionUID = 1L;

    protected MetricsTable metricsTable;

    protected Metric metric = null;

    public ProbedRestS3Service(AWSCredentials credentials)
            throws S3ServiceException {
        super(credentials);
    }

    protected void startMetric(String methodName) {
        if (metric == null) {
            metric = new Metric(getClass().getName(), methodName);
            getMetricsTable().addMetric(metric);
        } else {
            metric.addElement(methodName);
        }

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

    @Override
    public HttpConnectionManager getHttpConnectionManager() {
        startMetric("getHttpConnectionManager");
        HttpConnectionManager result = super.getHttpConnectionManager();
        stopMetric("getHttpConnectionManager");
        return result;
    }

    @Override
    public void setHttpConnectionManager(HttpConnectionManager httpConnectionManager) {
        startMetric("setHttpConnectionManager");
        super.setHttpConnectionManager(httpConnectionManager);
        stopMetric("setHttpConnectionManager");
    }

    @Override
    public HttpClient getHttpClient() {
        startMetric("getHttpClient");
        HttpClient result = super.getHttpClient();
        stopMetric("getHttpClient");
        return result;
    }

    @Override
    public void setHttpClient(HttpClient httpClient) {
        startMetric("setHttpClient");
        super.setHttpClient(httpClient);
        stopMetric("setHttpClient");
    }

    @Override
    public CredentialsProvider getCredentialsProvider() {
        startMetric("getCredentialsProvider");
        CredentialsProvider result = super.getCredentialsProvider();
        stopMetric("getCredentialsProvider");
        return result;
    }

    @Override
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        startMetric("setCredentialsProvider");
        super.setCredentialsProvider(credentialsProvider);
        stopMetric("setCredentialsProvider");
    }

    @Override
    public void authorizeHttpRequest(HttpMethod httpMethod) throws Exception {
        startMetric("authorizeHttpRequest");
        super.authorizeHttpRequest(httpMethod);
        stopMetric("authorizeHttpRequest");
    }

    @Override
    public boolean isBucketAccessible(String bucketName)
            throws S3ServiceException {
        startMetric("isBucketAccessible");
        boolean result = super.isBucketAccessible(bucketName);
        stopMetric("isBucketAccessible");
        return result;
    }

    @Override
    public int checkBucketStatus(String bucketName) throws S3ServiceException {
        startMetric("checkBucketStatus");
        int result = super.checkBucketStatus(bucketName);
        stopMetric("checkBucketStatus");
        return result;
    }

    @Override
    public S3Object putObjectWithSignedUrl(String signedPutUrl, S3Object object)
            throws S3ServiceException {
        startMetric("putObjectWithSignedUrl");
        S3Object result = super.putObjectWithSignedUrl(signedPutUrl, object);
        stopMetric("putObjectWithSignedUrl");
        return result;
    }

    @Override
    public void deleteObjectWithSignedUrl(String signedDeleteUrl)
            throws S3ServiceException {
        startMetric("deleteObjectWithSignedUrl");
        super.deleteObjectWithSignedUrl(signedDeleteUrl);
        stopMetric("deleteObjectWithSignedUrl");
    }

    @Override
    public S3Object getObjectWithSignedUrl(String signedGetUrl)
            throws S3ServiceException {
        startMetric("getObjectWithSignedUrl");
        S3Object result = super.getObjectWithSignedUrl(signedGetUrl);
        stopMetric("getObjectWithSignedUrl");
        return result;
    }

    @Override
    public S3Object getObjectDetailsWithSignedUrl(String signedHeadUrl)
            throws S3ServiceException {
        startMetric("getObjectDetailsWithSignedUrl");
        S3Object result = super.getObjectDetailsWithSignedUrl(signedHeadUrl);
        stopMetric("getObjectDetailsWithSignedUrl");
        return result;
    }

    @Override
    public AccessControlList getObjectAclWithSignedUrl(String signedAclUrl)
            throws S3ServiceException {
        startMetric("getObjectAclWithSignedUrl");
        AccessControlList result =
                super.getObjectAclWithSignedUrl(signedAclUrl);
        stopMetric("getObjectAclWithSignedUrl");
        return result;
    }

    @Override
    public void putObjectAclWithSignedUrl(String signedAclUrl,
                                          AccessControlList acl)
            throws S3ServiceException {
        startMetric("putObjectAclWithSignedUrl");
        super.putObjectAclWithSignedUrl(signedAclUrl, acl);
        stopMetric("putObjectAclWithSignedUrl");
    }
}
