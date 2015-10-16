/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionResult;
import com.amazonaws.services.cloudfront.model.ListStreamingDistributionsRequest;
import com.amazonaws.services.cloudfront.model.StreamingDistributionConfig;
import com.amazonaws.services.cloudfront.model.StreamingDistributionList;
import com.amazonaws.services.cloudfront.model.StreamingDistributionSummary;
import com.amazonaws.services.cloudfront.model.UpdateStreamingDistributionRequest;
import com.amazonaws.services.s3.AmazonS3Client;

import org.duracloud.StorageTaskConstants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Jun 1, 2010
 */
public abstract class BaseStreamingTaskRunner implements TaskRunner {

    private final Logger log =
        LoggerFactory.getLogger(BaseStreamingTaskRunner.class);

    public static final String STREAMING_HOST_PROP =
        StorageProvider.PROPERTIES_STREAMING_HOST;
    public static final String STREAMING_TYPE_PROP =
        StorageProvider.PROPERTIES_STREAMING_TYPE;
    public static enum STREAMING_TYPE {OPEN, SECURE};
    public static final String S3_ORIGIN_SUFFIX = ".s3.amazonaws.com";
    public static final String S3_ORIGIN_OAI_PREFIX = "origin-access-identity/cloudfront/";

    protected static final int maxRetries = 8;

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;
    protected AmazonCloudFrontClient cfClient;
    protected String cfAccountId;
    protected String cfKeyId;
    protected String cfKeyPath;

    public abstract String getName();

    public abstract String performTask(String taskParameters);

    /*
     * Returns the first streaming distribution associated with a given bucket
     */
    protected StreamingDistributionSummary getExistingDistribution(String bucketName) {

        List<StreamingDistributionSummary> dists =
            getAllExistingDistributions(bucketName);
        if(dists.isEmpty()) {
            return null;
        } else {
            return dists.get(0);
        }
    }

    private boolean isDistFromBucket(String bucketName,
                                     StreamingDistributionSummary distSummary) {
        String bucketOrigin = bucketName + S3_ORIGIN_SUFFIX;
        return bucketOrigin.equals(distSummary.getS3Origin().getDomainName());
    }

    /*
     * Determines if a streaming distribution already exists for a given bucket
     */
    protected List<StreamingDistributionSummary> getAllExistingDistributions(String bucketName) {

        List<StreamingDistributionSummary> distListForBucket = new ArrayList<>();

        StreamingDistributionList distList =
            cfClient.listStreamingDistributions(new ListStreamingDistributionsRequest())
                    .getStreamingDistributionList();

        for(StreamingDistributionSummary distSummary : distList.getItems()) {
            if(isDistFromBucket(bucketName, distSummary)) {
                distListForBucket.add(distSummary);
            }
        }

        return distListForBucket;
    }

    /**
     * Enables or disables an existing distribution
     *
     * @param distId the ID of the distribution
     * @param enabled true to enable, false to disable
     */
    protected void setDistributionState(String distId, boolean enabled) {
        GetStreamingDistributionConfigResult result =
            cfClient.getStreamingDistributionConfig(
                new GetStreamingDistributionConfigRequest(distId));

        StreamingDistributionConfig distConfig =
            result.getStreamingDistributionConfig();
        distConfig.setEnabled(enabled);

        cfClient.updateStreamingDistribution(
            new UpdateStreamingDistributionRequest()
                .withStreamingDistributionConfig(distConfig)
                .withIfMatch(result.getETag())
                .withId(distId));
    }

    /*
     * Get a listing of items in a space
     */
    protected Iterator<String> getSpaceContents(String spaceId) {
        for(int i=0; i<maxRetries; i++) {
            try {
                return s3Provider.getSpaceContents(spaceId, null);
            } catch(Exception e) {
                log.warn("Exception encountered attempting to get contents " +
                         "for streaming space: " + spaceId +
                         ", error message: " + e.getMessage());
                wait(i);
            }
        }
        throw new DuraCloudRuntimeException("Exceeded retries attempting to " +
                                            "get space contents for " + spaceId);
    }

    /*
     * Updates the space properties to no longer include the
     * streaming host value (if the value existed there in the first place)
     */
    protected void removeStreamingHostFromSpaceProps(String spaceId) {
        // Update bucket tags to remove streaming host
        Map<String, String> spaceProps =
            s3Provider.getSpaceProperties(spaceId);
        if(spaceProps.containsKey(STREAMING_HOST_PROP)) {
            spaceProps.remove(STREAMING_HOST_PROP);
            spaceProps.remove(STREAMING_TYPE_PROP);
            unwrappedS3Provider.setNewSpaceProperties(spaceId, spaceProps);
        }
    }

    protected void wait(int index) {
        try {
            Thread.sleep(1000 * index);
        } catch(InterruptedException e) {
        }
    }

    protected void
        checkThatStreamingServiceIsEnabled(StorageProvider s3Provider,
                                           String spaceId,
                                           String taskName) {
        // Verify that streaming is enabled
        Map<String, String> spaceProperties =
            s3Provider.getSpaceProperties(spaceId);
        if (!spaceProperties.containsKey(StorageProvider.PROPERTIES_STREAMING_TYPE)) {
            throw new UnsupportedTaskException(taskName,
                "The " + taskName + " task can only be used after a space " +
                "has been configured to enable streaming. Use " +
                StorageTaskConstants.ENABLE_STREAMING_TASK_NAME +
                " to enable streaming on this space.");
        }
    }

}
