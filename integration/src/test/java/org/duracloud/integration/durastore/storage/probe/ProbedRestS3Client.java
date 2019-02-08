/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage.probe;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AbstractAmazonS3;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.DeleteVersionRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListBucketsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.SetBucketLoggingConfigurationRequest;
import com.amazonaws.services.s3.model.SetBucketVersioningConfigurationRequest;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.VersionListing;
import org.duracloud.common.util.metrics.Metric;
import org.duracloud.common.util.metrics.MetricException;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.common.util.metrics.MetricsTable;

/**
 * This class wraps an AmazonS3Client implementation, collecting timing metrics
 * while passing calls down.
 *
 * @author Bill Branan
 */
public class ProbedRestS3Client extends AbstractAmazonS3 implements AmazonS3, MetricsProbed {

    private static final long serialVersionUID = 1L;

    protected MetricsTable metricsTable;

    protected Metric metric = null;

    protected AmazonS3 s3Client;

    public ProbedRestS3Client(BasicAWSCredentials credentials) throws AmazonServiceException {
        this.s3Client = AmazonS3ClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .build();
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
            s3Client.listNextBatchOfVersions(previousVersionListing);
        stopMetric("listNextBatchOfVersions");
        return result;
    }

    @Override
    public VersionListing listVersions(String bucketName, String prefix)
        throws AmazonClientException {
        startMetric("listVersions");
        VersionListing result = s3Client.listVersions(bucketName, prefix);
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
        VersionListing result = s3Client.listVersions(bucketName,
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
        VersionListing result = s3Client.listVersions(listVersionsRequest);
        stopMetric("listVersions");
        return result;
    }

    @Override
    public ObjectListing listObjects(String bucketName)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = s3Client.listObjects(bucketName);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listObjects(String bucketName, String prefix)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = s3Client.listObjects(bucketName, prefix);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listObjects(ListObjectsRequest listObjectsRequest)
        throws AmazonClientException {
        startMetric("listObjects");
        ObjectListing result = s3Client.listObjects(listObjectsRequest);
        stopMetric("listObjects");
        return result;
    }

    @Override
    public ObjectListing listNextBatchOfObjects(
        ObjectListing previousObjectListing)
        throws AmazonClientException {
        startMetric("listNextBatchOfObjects");
        ObjectListing result =
            s3Client.listNextBatchOfObjects(previousObjectListing);
        stopMetric("listNextBatchOfObjects");
        return result;
    }

    @Override
    public Owner getS3AccountOwner()
        throws AmazonClientException {
        startMetric("getS3AccountOwner");
        Owner result = s3Client.getS3AccountOwner();
        stopMetric("getS3AccountOwner");
        return result;
    }

    @Override
    public List<Bucket> listBuckets(ListBucketsRequest listBucketsRequest)
        throws AmazonClientException {
        startMetric("listBuckets");
        List<Bucket> result = listBuckets(listBucketsRequest);
        stopMetric("listBuckets");
        return result;
    }

    @Override
    public List<Bucket> listBuckets()
        throws AmazonClientException {
        startMetric("listBuckets");
        List<Bucket> result = s3Client.listBuckets();
        stopMetric("listBuckets");
        return result;
    }

    @Override
    public String getBucketLocation(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketLocation");
        String result = s3Client.getBucketLocation(bucketName);
        stopMetric("getBucketLocation");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = s3Client.createBucket(bucketName);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName, Region region)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = s3Client.createBucket(bucketName, region);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(String bucketName, String region)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = s3Client.createBucket(bucketName, region);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public Bucket createBucket(CreateBucketRequest createBucketRequest)
        throws AmazonClientException {
        startMetric("createBucket");
        Bucket result = s3Client.createBucket(createBucketRequest);
        stopMetric("createBucket");
        return result;
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObjectAcl");
        AccessControlList result = s3Client.getObjectAcl(bucketName, key);
        stopMetric("getObjectAcl");
        return result;
    }

    @Override
    public AccessControlList getObjectAcl(String bucketName, String key,
                                          String versionId)
        throws AmazonClientException {
        startMetric("getObjectAcl");
        AccessControlList result =
            s3Client.getObjectAcl(bucketName, key, versionId);
        stopMetric("getObjectAcl");
        return result;
    }

    @Override
    public void setObjectAcl(String bucketName, String key,
                             AccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        s3Client.setObjectAcl(bucketName, key, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key,
                             CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        s3Client.setObjectAcl(bucketName, key, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId,
                             AccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        s3Client.setObjectAcl(bucketName, key, versionId, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public void setObjectAcl(String bucketName, String key, String versionId,
                             CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setObjectAcl");
        s3Client.setObjectAcl(bucketName, key, versionId, acl);
        stopMetric("setObjectAcl");
    }

    @Override
    public AccessControlList getBucketAcl(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketAcl");
        AccessControlList result = s3Client.getBucketAcl(bucketName);
        stopMetric("getBucketAcl");
        return result;
    }

    @Override
    public void setBucketAcl(String bucketName, AccessControlList acl)
        throws AmazonClientException {
        startMetric("setBucketAcl");
        s3Client.setBucketAcl(bucketName, acl);
        stopMetric("setBucketAcl");
    }

    @Override
    public void setBucketAcl(String bucketName, CannedAccessControlList acl)
        throws AmazonClientException {
        startMetric("setBucketAcl");
        s3Client.setBucketAcl(bucketName, acl);
        stopMetric("setBucketAcl");
    }

    @Override
    public ObjectMetadata getObjectMetadata(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObjectMetadata");
        ObjectMetadata result = s3Client.getObjectMetadata(bucketName, key);
        stopMetric("getObjectMetadata");
        return result;
    }

    @Override
    public ObjectMetadata getObjectMetadata(
        GetObjectMetadataRequest getObjectMetadataRequest)
        throws AmazonClientException {
        startMetric("getObjectMetadata");
        ObjectMetadata result =
            s3Client.getObjectMetadata(getObjectMetadataRequest);
        stopMetric("getObjectMetadata");
        return result;
    }

    @Override
    public S3Object getObject(String bucketName, String key)
        throws AmazonClientException {
        startMetric("getObject");
        S3Object result = s3Client.getObject(bucketName, key);
        stopMetric("getObject");
        return result;
    }

    @Override
    public boolean doesBucketExist(String bucketName)
        throws AmazonClientException {
        startMetric("doesBucketExist");
        boolean result = s3Client.doesBucketExist(bucketName);
        stopMetric("doesBucketExist");
        return result;
    }

    @Override
    public void changeObjectStorageClass(String bucketName, String key,
                                         StorageClass newStorageClass)
        throws AmazonClientException {
        startMetric("changeObjectStorageClass");
        s3Client.changeObjectStorageClass(bucketName, key, newStorageClass);
        stopMetric("changeObjectStorageClass");
    }

    @Override
    public S3Object getObject(GetObjectRequest getObjectRequest)
        throws AmazonClientException {
        startMetric("getObject");
        S3Object result = s3Client.getObject(getObjectRequest);
        stopMetric("getObject");
        return result;
    }

    @Override
    public ObjectMetadata getObject(GetObjectRequest getObjectRequest,
                                    File destinationFile)
        throws AmazonClientException {
        startMetric("getObject");
        ObjectMetadata result =
            s3Client.getObject(getObjectRequest, destinationFile);
        stopMetric("getObject");
        return result;
    }

    @Override
    public void deleteBucket(String bucketName)
        throws AmazonClientException {
        startMetric("deleteBucket");
        s3Client.deleteBucket(bucketName);
        stopMetric("deleteBucket");
    }

    @Override
    public void deleteBucket(DeleteBucketRequest deleteBucketRequest)
        throws AmazonClientException {
        startMetric("deleteBucket");
        s3Client.deleteBucket(deleteBucketRequest);
        stopMetric("deleteBucket");
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key, File file)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result = s3Client.putObject(bucketName, key, file);
        stopMetric("putObject");
        return result;
    }

    @Override
    public PutObjectResult putObject(String bucketName, String key,
                                     InputStream input, ObjectMetadata metadata)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result =
            s3Client.putObject(bucketName, key, input, metadata);
        stopMetric("putObject");
        return result;
    }

    @Override
    public PutObjectResult putObject(PutObjectRequest putObjectRequest)
        throws AmazonClientException {
        startMetric("putObject");
        PutObjectResult result = s3Client.putObject(putObjectRequest);
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
        CopyObjectResult result = s3Client.copyObject(sourceBucketName,
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
        CopyObjectResult result = s3Client.copyObject(copyObjectRequest);
        stopMetric("copyObject");
        return result;
    }

    @Override
    public void deleteObject(String bucketName, String key)
        throws AmazonClientException {
        startMetric("deleteObject");
        s3Client.deleteObject(bucketName, key);
        stopMetric("deleteObject");
    }

    @Override
    public void deleteObject(DeleteObjectRequest deleteObjectRequest)
        throws AmazonClientException {
        startMetric("deleteObject");
        s3Client.deleteObject(deleteObjectRequest);
        stopMetric("deleteObject");
    }

    @Override
    public void deleteVersion(String bucketName, String key, String versionId)
        throws AmazonClientException {
        startMetric("deleteVersion");
        s3Client.deleteVersion(bucketName, key, versionId);
        stopMetric("deleteVersion");
    }

    @Override
    public void deleteVersion(DeleteVersionRequest deleteVersionRequest)
        throws AmazonClientException {
        startMetric("deleteVersion");
        s3Client.deleteVersion(deleteVersionRequest);
        stopMetric("deleteVersion");
    }

    @Override
    public void setBucketVersioningConfiguration(
        SetBucketVersioningConfigurationRequest setBucketVersioningConfigurationRequest)
        throws AmazonClientException {
        startMetric("setBucketVersioningConfiguration");
        s3Client.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest);
        stopMetric("setBucketVersioningConfiguration");
    }

    @Override
    public BucketVersioningConfiguration getBucketVersioningConfiguration(
        String bucketName)
        throws AmazonClientException {
        startMetric("getBucketVersioningConfiguration");
        BucketVersioningConfiguration result =
            s3Client.getBucketVersioningConfiguration(bucketName);
        stopMetric("getBucketVersioningConfiguration");
        return result;
    }

    @Override
    public void setBucketNotificationConfiguration(String bucketName,
                                                   BucketNotificationConfiguration bucketNotificationConfiguration)
        throws AmazonClientException {
        startMetric("setBucketNotificationConfiguration");
        s3Client.setBucketNotificationConfiguration(bucketName,
                                                 bucketNotificationConfiguration);
        stopMetric("setBucketNotificationConfiguration");
    }

    @Override
    public BucketNotificationConfiguration getBucketNotificationConfiguration(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketNotificationConfiguration");
        BucketNotificationConfiguration result =
            s3Client.getBucketNotificationConfiguration(bucketName);
        stopMetric("getBucketNotificationConfiguration");
        return result;
    }

    @Override
    public BucketLoggingConfiguration getBucketLoggingConfiguration(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketLoggingConfiguration");
        BucketLoggingConfiguration result =
            s3Client.getBucketLoggingConfiguration(bucketName);
        stopMetric("getBucketLoggingConfiguration");
        return result;
    }

    @Override
    public void setBucketLoggingConfiguration(
        SetBucketLoggingConfigurationRequest setBucketLoggingConfigurationRequest)
        throws AmazonClientException {
        startMetric("setBucketLoggingConfiguration");
        s3Client.setBucketLoggingConfiguration(
            setBucketLoggingConfigurationRequest);
        stopMetric("setBucketLoggingConfiguration");
    }

    @Override
    public BucketPolicy getBucketPolicy(String bucketName)
        throws AmazonClientException {
        startMetric("getBucketPolicy");
        BucketPolicy result = s3Client.getBucketPolicy(bucketName);
        stopMetric("getBucketPolicy");
        return result;
    }

    @Override
    public void setBucketPolicy(String bucketName, String policyText)
        throws AmazonClientException {
        startMetric("setBucketPolicy");
        s3Client.setBucketPolicy(bucketName, policyText);
        stopMetric("setBucketPolicy");
    }

    @Override
    public void deleteBucketPolicy(String bucketName)
        throws AmazonClientException {
        startMetric("deleteBucketPolicy");
        s3Client.deleteBucketPolicy(bucketName);
        stopMetric("deleteBucketPolicy");
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key, Date expiration)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result = s3Client.generatePresignedUrl(bucketName, key, expiration);
        stopMetric("generatePresignedUrl");
        return result;
    }

    @Override
    public URL generatePresignedUrl(String bucketName, String key,
                                    Date expiration,
                                    com.amazonaws.HttpMethod method)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result = s3Client.generatePresignedUrl(bucketName, key, expiration, method);
        stopMetric("generatePresignedUrl");
        return result;
    }

    @Override
    public URL generatePresignedUrl(
        GeneratePresignedUrlRequest generatePresignedUrlRequest)
        throws AmazonClientException {
        startMetric("generatePresignedUrl");
        URL result = s3Client.generatePresignedUrl(generatePresignedUrlRequest);
        stopMetric("generatePresignedUrl");
        return result;
    }

}
