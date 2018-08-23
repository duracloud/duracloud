/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.HLS_STREAMING_HOST_PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import org.duracloud.common.constant.Constants;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedCookiesUrlTaskResult;
import org.duracloud.s3storageprovider.dto.SignedCookieData;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 22, 2018
 */
public class GetHlsSignedCookiesUrlTaskRunnerTest extends HlsTaskRunnerTestBase {

    @Before
    public void setup() {
        super.setup();
        cfKeyPath = this.getClass().getClassLoader()
                        .getResource("test-signing-key.der").getPath();
    }

    @Test
    public void testGetName() {
        GetHlsSignedCookiesUrlTaskRunner runner =
            new GetHlsSignedCookiesUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient,
                                                 dataStoreFactory, cfKeyId, cfKeyPath);

        replayMocks();

        String name = runner.getName();
        assertEquals("get-signed-cookies-url", name);
    }

    /*
     * Testing the case where a distribution domain is not listed in space properties
     * (mostly likely meaning streaming is not enabled), an exception is expected
     */
    @Test
    public void testPerformTaskNoDistributionDomain() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(new HashMap<>());

        GetHlsSignedCookiesUrlTaskRunner runner =
            new GetHlsSignedCookiesUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient,
                                                 dataStoreFactory, cfKeyId, cfKeyPath);
        // Replay mocks
        replayMocks();

        // Verify failure on null parameters
        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }

        // Verify failure when the space does not have an associated distribution
        GetSignedCookiesUrlTaskParameters taskParams = new GetSignedCookiesUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setRedirectUrl(redirectUrl);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * Testing the case where a distribution does not exist for the given bucket,
     * an exception is expected
     */
    @Test
    public void testPerformTaskNoDistribution() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props);

        // Empty distribution list
        ListDistributionsResult distSummaryResult =
            new ListDistributionsResult().withDistributionList(new DistributionList());
        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(distSummaryResult);

        GetHlsSignedCookiesUrlTaskRunner runner =
            new GetHlsSignedCookiesUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient,
                                                 dataStoreFactory, cfKeyId, cfKeyPath);
        // Replay mocks
        replayMocks();

        // Verify failure when the space does not have an associated distribution
        GetSignedCookiesUrlTaskParameters taskParams = new GetSignedCookiesUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setRedirectUrl(redirectUrl);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and a signed cookies URL is successfully generated.
     */
    @Test
    public void testPerformTaskSuccess() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props);

        cfClientExpectValidDistribution(cfClient);

        String token = "abc123";

        EasyMock.expect(dataStoreFactory.create(Constants.HIDDEN_COOKIE_SPACE))
                .andReturn(dataStore);
        Capture<String> cookiesDataCapture = Capture.newInstance();
        EasyMock.expect(dataStore.storeData(EasyMock.capture(cookiesDataCapture)))
                .andReturn(token);

        GetHlsSignedCookiesUrlTaskRunner runner =
            new GetHlsSignedCookiesUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient,
                                                 dataStoreFactory, cfKeyId, cfKeyPath);
        // Replay mocks
        replayMocks();

        // Verify success
        GetSignedCookiesUrlTaskParameters taskParams = new GetSignedCookiesUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setMinutesToExpire(100);
        taskParams.setIpAddress("1.2.3.4");
        taskParams.setRedirectUrl(redirectUrl);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        GetSignedCookiesUrlTaskResult taskResult = GetSignedCookiesUrlTaskResult.deserialize(results);
        assertNotNull(taskResult);

        String cookieUrl = taskResult.getSignedCookiesUrl();
        assertNotNull(cookieUrl);
        assertEquals("https://" + domainName + "/cookies?token=" + token, cookieUrl);

        // Verify stored data (cookies)
        String cookiesData = cookiesDataCapture.getValue();
        SignedCookieData signedCookieData = SignedCookieData.deserialize(cookiesData);
        assertNotNull(signedCookieData);
        assertEquals(domainName, signedCookieData.getStreamingHost());
        assertEquals(redirectUrl, signedCookieData.getRedirectUrl());

        Map<String, String> signedCookies = signedCookieData.getSignedCookies();
        assertNotNull(signedCookies);
        assertNotNull(signedCookies.get("CloudFront-Policy"));
        assertNotNull(signedCookies.get("CloudFront-Signature"));
        assertNotNull(signedCookies.get("CloudFront-Key-Pair-Id"));
    }

}
