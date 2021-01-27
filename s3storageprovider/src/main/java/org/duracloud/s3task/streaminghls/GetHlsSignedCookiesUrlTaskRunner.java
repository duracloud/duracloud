/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.CloudFrontCookieSigner;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import org.duracloud.StorageTaskConstants;
import org.duracloud.common.constant.Constants;
import org.duracloud.common.util.IOUtil;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StringDataStore;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskResult;
import org.duracloud.s3storageprovider.dto.SignedCookieData;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Retrieves a URL which can be used to set signed cookies on the user's browser.
 * These cookies allow access to content that is streamed through Amazon Cloudfront
 * via a secure HLS distribution
 *
 * @author: Bill Branan
 * Date: Aug 6, 2018
 */
public class GetHlsSignedCookiesUrlTaskRunner extends BaseHlsTaskRunner {

    public static final int DEFAULT_MINUTES_TO_EXPIRE = 480;

    private final Logger log = LoggerFactory.getLogger(GetHlsSignedCookiesUrlTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME;

    private StringDataStoreFactory dataStoreFactory;

    public GetHlsSignedCookiesUrlTaskRunner(StorageProvider s3Provider,
                                            S3StorageProvider unwrappedS3Provider,
                                            AmazonCloudFrontClient cfClient,
                                            StringDataStoreFactory dataStoreFactory,
                                            String cfKeyId,
                                            String cfKeyPath) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.cfClient = cfClient;
        this.dataStoreFactory = dataStoreFactory;

        // Certificate identifier, an active trusted signer for the distribution
        this.cfKeyId = cfKeyId;
        // Local file path to signing key in DER format
        this.cfKeyPath = cfKeyPath.trim();
    }

    public String getName() {
        return TASK_NAME;
    }

    // Create and store signed cookies
    public String performTask(String taskParameters) {
        GetSignedCookiesUrlTaskParameters taskParams =
            GetSignedCookiesUrlTaskParameters.deserialize(taskParameters);

        String spaceId = taskParams.getSpaceId();
        String ipAddress = taskParams.getIpAddress();
        int minutesToExpire = taskParams.getMinutesToExpire();
        if (minutesToExpire <= 0) {
            minutesToExpire = DEFAULT_MINUTES_TO_EXPIRE;
        }
        String redirectUrl = taskParams.getRedirectUrl();

        log.info("Performing " + TASK_NAME + " task with parameters: spaceId=" + spaceId +
                 ", minutesToExpire=" + minutesToExpire + ", ipAddress=" + ipAddress +
                 ", redirectUrl=" + redirectUrl);

        // Will throw if bucket does not exist
        String bucketName = unwrappedS3Provider.getBucketName(spaceId);

        // Ensure that streaming service is on
        checkThatStreamingServiceIsEnabled(spaceId, TASK_NAME);

        // Retrieve the existing distribution for the given space
        DistributionSummary existingDist = getExistingDistribution(bucketName);
        if (null == existingDist) {
            throw new UnsupportedTaskException(TASK_NAME,
                                               "The " + TASK_NAME + " task can only be used after a space " +
                                               "has been configured to enable secure streaming. Use " +
                                               StorageTaskConstants.ENABLE_HLS_TASK_NAME +
                                               " to enable secure streaming on this space.");
        }
        String domainName = existingDist.getDomainName();

        // Define expiration date/time
        Calendar expireCalendar = Calendar.getInstance();
        expireCalendar.add(Calendar.MINUTE, minutesToExpire);

        Map<String, String> signedCookies = new HashMap<>();
        try {
            File cfKeyPathFile = getCfKeyPathFile(this.cfKeyPath);

            // Generate signed cookies
            CloudFrontCookieSigner.CookiesForCustomPolicy cookies =
                CloudFrontCookieSigner.getCookiesForCustomPolicy(
                    SignerUtils.Protocol.https,
                    domainName,
                    cfKeyPathFile,
                    "*",
                    cfKeyId,
                    expireCalendar.getTime(),
                    null,
                    ipAddress);

            signedCookies.put(cookies.getPolicy().getKey(), cookies.getPolicy().getValue());
            signedCookies.put(cookies.getSignature().getKey(), cookies.getSignature().getValue());
            signedCookies.put(cookies.getKeyPairId().getKey(), cookies.getKeyPairId().getValue());
        } catch (InvalidKeySpecException | IOException e) {
            throw new RuntimeException("Error encountered attempting to create signed cookies in task " +
                                       TASK_NAME + ": " + e.getMessage(), e);
        }

        String token = storeCookies(signedCookies, domainName, redirectUrl);

        GetSignedCookiesUrlTaskResult taskResult = new GetSignedCookiesUrlTaskResult();
        taskResult.setSignedCookiesUrl("https://" + domainName + "/cookies?token=" + token);

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

    private File getCfKeyPathFile(String cfKeyPath) throws IOException {
        if (cfKeyPath.startsWith("s3://")) {
            File keyFile = new File(System.getProperty("java.io.tmpdir"),
                                    "cloudfront-key.der");
            if (!keyFile.exists()) {
                Resource resource = S3ProviderUtil.getS3ObjectByUrl(cfKeyPath);
                File tmpFile = IOUtil.writeStreamToFile(resource.getInputStream());
                tmpFile.renameTo(keyFile);
                keyFile.deleteOnExit();
            }

            return keyFile;
        } else {
            return new File(cfKeyPath);
        }
    }

    /*
     * Stores signed cookies data and returns a token which can be used
     * to retrieve the data from the /aux/cookies DuraStore path
     */
    private String storeCookies(Map<String, String> signedCookies,
                                String streamingHost,
                                String redirectUrl) {
        SignedCookieData signedCookieData = new SignedCookieData();
        signedCookieData.setSignedCookies(signedCookies);
        signedCookieData.setStreamingHost(streamingHost);
        signedCookieData.setRedirectUrl(redirectUrl);

        String cookiesData = signedCookieData.serialize();

        StringDataStore signedCookieStore = dataStoreFactory.create(Constants.HIDDEN_COOKIE_SPACE);
        return signedCookieStore.storeData(cookiesData);
    }

}
