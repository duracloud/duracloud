/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.UpdateDistributionRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.StorageTaskConstants;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides shared capabilities to support HLS streaming
 *
 * @author: Bill Branan
 * Date: Aug 3, 2018
 */
public abstract class BaseHlsTaskRunner implements TaskRunner {

    private final Logger log = LoggerFactory.getLogger(BaseHlsTaskRunner.class);

    public static final String HLS_STREAMING_HOST_PROP = StorageProvider.PROPERTIES_HLS_STREAMING_HOST;
    public static final String HLS_STREAMING_TYPE_PROP = StorageProvider.PROPERTIES_HLS_STREAMING_TYPE;

    public enum STREAMING_TYPE { OPEN, SECURE }

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

    /**
     * Returns the first streaming web distribution associated with a given bucket
     */
    protected DistributionSummary getExistingDistribution(String bucketName) {

        List<DistributionSummary> dists = getAllExistingWebDistributions(bucketName);
        if (dists.isEmpty()) {
            return null;
        } else {
            return dists.get(0);
        }
    }

    private boolean isDistFromBucket(String bucketName, DistributionSummary distSummary) {
        String bucketOrigin = bucketName + S3_ORIGIN_SUFFIX;
        for (Origin distOrigin : distSummary.getOrigins().getItems()) {
            if (bucketOrigin.equals(distOrigin.getDomainName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a streaming distribution already exists for a given bucket
     */
    protected List<DistributionSummary> getAllExistingWebDistributions(String bucketName) {

        List<DistributionSummary> distListForBucket = new ArrayList<>();

        DistributionList distList =
            cfClient.listDistributions(new ListDistributionsRequest())
                    .getDistributionList();

        List<DistributionSummary> webDistList = distList.getItems();
        while (distList.isTruncated()) {
            distList = cfClient.listDistributions(
                new ListDistributionsRequest().withMarker(distList.getNextMarker()))
                               .getDistributionList();
            webDistList.addAll(distList.getItems());
        }

        for (DistributionSummary distSummary : webDistList) {
            if (isDistFromBucket(bucketName, distSummary)) {
                distListForBucket.add(distSummary);
            }
        }

        return distListForBucket;
    }

    /**
     * Enables or disables an existing distribution
     *
     * @param distId  the ID of the distribution
     * @param enabled true to enable, false to disable
     */
    protected void setDistributionState(String distId, boolean enabled) {
        GetDistributionConfigResult result =
            cfClient.getDistributionConfig(new GetDistributionConfigRequest(distId));

        DistributionConfig distConfig = result.getDistributionConfig();
        distConfig.setEnabled(enabled);

        cfClient.updateDistribution(new UpdateDistributionRequest()
                                        .withDistributionConfig(distConfig)
                                        .withIfMatch(result.getETag())
                                        .withId(distId));
    }

    /**
     * Get a listing of items in a space
     */
    protected Iterator<String> getSpaceContents(String spaceId) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                return s3Provider.getSpaceContents(spaceId, null);
            } catch (Exception e) {
                log.warn("Exception encountered attempting to get contents for space: " +
                         spaceId + ", error message: " + e.getMessage());
                wait(i);
            }
        }
        throw new DuraCloudRuntimeException("Exceeded retries attempting to " +
                                            "get space contents for " + spaceId);
    }

    /**
     * Updates the space properties to no longer include the
     * streaming host value (if the value existed there in the first place)
     */
    protected void removeHlsStreamingHostFromSpaceProps(String spaceId) {
        // Update bucket tags to remove streaming host
        Map<String, String> spaceProps = s3Provider.getSpaceProperties(spaceId);
        if (spaceProps.containsKey(HLS_STREAMING_HOST_PROP) || spaceProps.containsKey(HLS_STREAMING_TYPE_PROP)) {
            spaceProps.remove(HLS_STREAMING_HOST_PROP);
            spaceProps.remove(HLS_STREAMING_TYPE_PROP);
            unwrappedS3Provider.setNewSpaceProperties(spaceId, spaceProps);
        }
    }

    protected void wait(int index) {
        try {
            Thread.sleep(1000 * index);
        } catch (InterruptedException e) {
            // Exit sleep on interruption
        }
    }

    /**
     * Determines if a streaming distribution exists for a given space
     *
     * @throws UnsupportedTaskException if no distribution exists
     */
    protected void checkThatStreamingServiceIsEnabled(String spaceId, String taskName) {
        // Verify that streaming is enabled
        Map<String, String> spaceProperties = s3Provider.getSpaceProperties(spaceId);
        if (!spaceProperties.containsKey(HLS_STREAMING_HOST_PROP)) {
            throw new UnsupportedTaskException(
                taskName,
                "The " + taskName + " task can only be used after a space " +
                "has been configured to enable HLS streaming. Use " +
                StorageTaskConstants.ENABLE_HLS_TASK_NAME +
                " to enable HLS streaming on this space.");
        }
    }

}
