/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage.probe;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * This class wraps an AmazonS3Client implementation, collecting timing metrics
 * while passing calls down.
 *
 * @author Bill Branan
 */
public class ProbedRestS3Client
        extends AmazonS3Client
        implements MetricsProbed {

    private static final long serialVersionUID = 1L;

    protected MetricsTable metricsTable;

    protected Metric metric = null;

    public ProbedRestS3Client(AWSCredentials credentials)
        throws AmazonServiceException {
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
            throw new RuntimeException(
                new MetricException("Metrics table has not been set."));
        }
        return this.metricsTable;
    }

    @Override
    public VersionListing listNextBatchOfVersions(
        VersionListing previousVersionListing)
        throws AmazonClientException {
        startMetric("listNextBatchOfVersions");
        VersionListing result =
            super.listNextBatchOfVersions(previousVersionListing);
        stopMetric("listNextBatchOfVersions");
        return result;
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix)
        throws AmazonClientException {
        startMetric("listVersions");
        VersionListing result =  super.listVersions(bucketName, prefix);
        stopMetric("listVersions");
        return result;
    }

    @Override
    public VersionListing listVersions(String bucketName,
                                       String prefix,
                                       String keyMarker,
                                       String versionIdMarker,
                                       String delimiter,
                                       Integer maxKeys)
        throws AmazonClientException {
        startMetric("listVersions");
        VersionListing result =  super.listVersions(bucketName,
                                                    prefix,
                                                    keyMarker,
                                                    versionIdMarker,
                                                    delimiter,
                                                    maxKeys);
        stopMetric("listVersions");
        return result;
    }

    @Override
    public VersionListing listVersions(ListVersionsRequest listVersionsRequest)
        throws AmazonClientException {
        startMetric("listVersions");
        VersionListing result =  super.listVersions(listVersionsRequest);
        stopMetric("listVersions");
        return result;
    }

    @Override
    public ObjectListing listObjects(String bucketName)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = super.listObjects(bucketName);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listObjects(String bucketName, String prefix)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = super.listObjects(bucketName, prefix);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = super.listObjects(listObjectsRequest);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(
        ObjectListing previousObjectListing)
        throws AmazonClientException {
        startMetric("listNextBatchOfObjects");
        ObjectListing result =
            super.listNextBatchOfObjects(previousObjectListing);
        stopMetric("listNextBatchOfObjects");
        return result;
    }

    @Override
    public Owner getS3AccountOwner()
        throws AmazonClientException {
        startMetric("getS3AccountOwner");
        Owner result = super.getS3AccountOwner();
        stopMetric("getS3AccountOwner");
        return result;
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest)
        throws AmazonClientException {
        startMetric("listBuckets");
        List<Bucket> result = super.listBuckets(listBucketsRequest);
        stopMetric("listBuckets");
        return result;
    }

    @Override
    public List<Bucket> listBuckets()
        throws AmazonClientException {
        startMetric("listBuckets");
        List<Bucket> result = super.listBuckets();
        stopMetric("listBuckets");
        return result;
    }

    @Override
    public String getBucketLocation(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketLocation");
        String result = super.getBucketLocation(bucketName);
        stopMetric("getBucketLocation");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = super.createBucket(bucketName);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName, Region region)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = super.createBucket(bucketName, region);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName, String region)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = super.createBucket(bucketName, region);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = super.createBucket(createBucketRequest);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObjectAcl");
        AccessControlList result = super.getObjectAcl(bucketName, key);
        stopMetric("getObjectAcl");
        return result;
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key,
                                          String versionId)
        throws AmazonClientException {
        startMetric("getObjectAcl");
        AccessControlList result =
            super.getObjectAcl(bucketName, key, versionId);
        stopMetric("getObjectAcl");
        return result;
    }

    @Override
    public void setObjectAcl(String bucketName, String key,
                             AccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        super.setObjectAcl(bucketName, key, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key,
                             CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        super.setObjectAcl(bucketName, key, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId,
                             AccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        super.setObjectAcl(bucketName, key, versionId, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId,
                             CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        super.setObjectAcl(bucketName, key, versionId, acl); 
        stopMetric("setObjectAcl");
    }

    @Override
    public AccessControlList getBucketAcl(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketAcl");
        AccessControlList result = super.getBucketAcl(bucketName);
        stopMetric("getBucketAcl");
        return result;
    }

    @Override
    public void setBucketAcl(String bucketName, AccessControlList acl)
        throws AmazonClientException {
        startMetric("setBucketAcl");
        super.setBucketAcl(bucketName, acl);
        stopMetric("setBucketAcl");
    }

    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setBucketAcl");
        super.setBucketAcl(bucketName, acl);
        stopMetric("setBucketAcl");
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObjectMetadata");
        ObjectMetadata result = super.getObjectMetadata(bucketName, key);
        stopMetric("getObjectMetadata");
        return result;
    }

    @Override
    public ObjectMetadata getObjectMetadata(
        GetObjectMetadataRequest getObjectMetadataRequest)
        throws AmazonClientException {
        startMetric("getObjectMetadata");
        ObjectMetadata result =
            super.getObjectMetadata(getObjectMetadataRequest);
        stopMetric("getObjectMetadata");
        return result;
    }

    @Override
    public S3Object getObject(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObject");
        S3Object result = super.getObject(bucketName, key);
        stopMetric("getObject");
        return result;
    }

    @Override
    public boolean doesBucketExist(String bucketName)
        throws AmazonClientException {
        startMetric("doesBucketExist");
        boolean result = super.doesBucketExist(bucketName);
        stopMetric("doesBucketExist");
        return result;
    }

    @Override
    public void changeObjectStorageClass(String bucketName, String key,
                                         StorageClass newStorageClass)
        throws AmazonClientException {
        startMetric("changeObjectStorageClass");
        super.changeObjectStorageClass(bucketName, key, newStorageClass);
        stopMetric("changeObjectStorageClass");
    }

    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest)
        throws AmazonClientException {
        startMetric("getObject");
        S3Object result = super.getObject(getObjectRequest);
        stopMetric("getObject");
        return result;
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest,
                                    File destinationFile)
        throws AmazonClientException {
        startMetric("getObject");
        ObjectMetadata result =
            super.getObject(getObjectRequest, destinationFile);
        stopMetric("getObject");
        return result;
    }

    @Override
    public void deleteBucket(String bucketName)
        throws AmazonClientException {
        startMetric("deleteBucket");
        super.deleteBucket(bucketName);
        stopMetric("deleteBucket");
    }

    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest)
        throws AmazonClientException {
        startMetric("deleteBucket");
        super.deleteBucket(deleteBucketRequest);
        stopMetric("deleteBucket");
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key, File file)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result = super.putObject(bucketName, key, file);
        stopMetric("putObject");
        return result;
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key,
                                     InputStream input, ObjectMetadata metadata)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result =
            super.putObject(bucketName, key, input, metadata);
        stopMetric("putObject");
        return result;
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest putObjectRequest)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result = super.putObject(putObjectRequest); 
        stopMetric("putObject");
        return result;
    }

    @Override
    public CopyObjectResult copyObject(String sourceBucketName,
                                       String sourceKey,
                                       String destinationBucketName,
                                       String destinationKey)
        throws AmazonClientException {
        startMetric("copyObject");
        CopyObjectResult result = super.copyObject(sourceBucketName,
                                                   sourceKey,
                                                   destinationBucketName,
                                                   destinationKey);
        stopMetric("copyObject");
        return result;
    }

    @Override
    public CopyObjectResult copyObject(CopyObjectRequest copyObjectRequest)
        throws AmazonClientException {
        startMetric("copyObject");
        CopyObjectResult result = super.copyObject(copyObjectRequest); 
        stopMetric("copyObject");
        return result;
    }

    @Override
    public void deleteObject(String bucketName, String key)
        throws AmazonClientException {
        startMetric("deleteObject");
        super.deleteObject(bucketName, key); 
        stopMetric("deleteObject");
    }

    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest)
        throws AmazonClientException {
        startMetric("deleteObject");
        super.deleteObject(deleteObjectRequest);
        stopMetric("deleteObject");
    }

    @Override
    public void deleteVersion(String bucketName, String key, String versionId)
        throws AmazonClientException {
        startMetric("deleteVersion");
        super.deleteVersion(bucketName, key, versionId);
        stopMetric("deleteVersion");
    }

    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest)
        throws AmazonClientException {
        startMetric("deleteVersion");
        super.deleteVersion(deleteVersionRequest);
        stopMetric("deleteVersion");
    }

    @Override
    public void setBucketVersioningConfiguration(
        SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest)
        throws AmazonClientException {
        startMetric("setBucketVersioningConfiguration");
        super.setBucketVersioningConfiguration(
            setBucketVersioningConfigurationRequest); 
        stopMetric("setBucketVersioningConfiguration");
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(
        String bucketName)
        throws AmazonClientException {
        startMetric("getBucketVersioningConfiguration");
        BucketVersioningConfiguration result =
            super.getBucketVersioningConfiguration(bucketName);
        stopMetric("getBucketVersioningConfiguration");
        return result;
    }

    @Override
    public void setBucketNotificationConfiguration(String bucketName,
                                                   BucketNotificationConfiguration bucketNotificationConfiguration)
        throws AmazonClientException {
        startMetric("setBucketNotificationConfiguration");
        super.setBucketNotificationConfiguration(bucketName,
                                                 bucketNotificationConfiguration); 
        stopMetric("setBucketNotificationConfiguration");
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(
        String bucketName)
        throws AmazonClientException {
        startMetric("getBucketNotificationConfiguration");
        BucketNotificationConfiguration result =
            super.getBucketNotificationConfiguration(bucketName);
        stopMetric("getBucketNotificationConfiguration");
        return result;
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(
        String bucketName)
        throws AmazonClientException {
        startMetric("getBucketLoggingConfiguration");
        BucketLoggingConfiguration result =
            super.getBucketLoggingConfiguration(bucketName);
        stopMetric("getBucketLoggingConfiguration");
        return result;
    }

    @Override
    public void setBucketLoggingConfiguration(
        SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest)
        throws AmazonClientException {
        startMetric("setBucketLoggingConfiguration");
        super.setBucketLoggingConfiguration(
            setBucketLoggingConfigurationRequest); 
        stopMetric("setBucketLoggingConfiguration");
    }

    @Override
    public BucketPolicy getBucketPolicy(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketPolicy");
        BucketPolicy result = super.getBucketPolicy(bucketName);
        stopMetric("getBucketPolicy");
        return result;
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyText)
        throws AmazonClientException {
        startMetric("setBucketPolicy");
        super.setBucketPolicy(bucketName, policyText); 
        stopMetric("setBucketPolicy");
    }

    @Override
    public void deleteBucketPolicy(String bucketName)
        throws AmazonClientException {
        startMetric("deleteBucketPolicy");
        super.deleteBucketPolicy(bucketName);
        stopMetric("deleteBucketPolicy");
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key,
                                    Date expiration)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result = super.generatePresignedUrl(bucketName, key, expiration);
        stopMetric("generatePresignedUrl");
        return result;
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key,
                                    Date expiration,
                                    com.amazonaws.HttpMethod method)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result =
            super.generatePresignedUrl(bucketName, key, expiration, method);
        stopMetric("generatePresignedUrl");
        return result;
    }

    @Override
    public URL generatePresignedUrl(
        GeneratePresignedUrlRequest generatePresignedUrlRequest)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result = super.generatePresignedUrl(generatePresignedUrlRequest); 
        stopMetric("generatePresignedUrl");
        return result;
    }

}