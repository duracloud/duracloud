/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

import org.duracloud.common.util.DateUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class FixityServiceOptionsTest {

    private FixityServiceOptions serviceOptions;
    private Map<String, String> params;

    private String modeKey = "modeKey";
    private String hashApproachKey = "hashApproachKey";
    private String saltKey = "saltKey";
    private String failFastKey = "failFastKey";
    private String storeIdKey = "storeIdKey";
    private String providedListingSpaceIdAKey = "providedListingSpaceIdAKey";
    private String providedListingSpaceIdBKey = "providedListingSpaceIdBKey";
    private String providedListingContentIdAKey = "providedListingContentIdAKey";
    private String providedListingContentIdBKey = "providedListingContentIdBKey";
    private String targetSpaceIdKey = "targetSpaceIdKey";
    private String outputSpaceIdKey = "outputSpaceIdKey";
    private String outputContentIdKey = "outputContentIdKey";
    private String reportContentIdKey = "reportContentIdKey";

    private String mode = FixityServiceOptions.Mode.GENERATE_LIST.getKey();
    private String hashApproach = FixityServiceOptions.HashApproach
        .SALTED
        .name();
    private String salt = "abc123";
    private String failFast = Boolean.FALSE.toString();
    private String storeId = "1";
    private String providedListingSpaceIdA = "spaceIdA";
    private String providedListingSpaceIdB = "spaceIdB";
    private String providedListingContentIdA = "contentIdA";
    private String providedListingContentIdB = "contentIdB";
    private String targetSpaceId = "targetSpaceId";
    private String outputSpaceId = "outputSpaceId";
    private String outputContentId = "outputContentId";
    private String reportContentId = "reportContentId";

    @Before
    public void setUp() throws Exception {
        params = new HashMap<String, String>();
        params.put(modeKey, mode);
        params.put(hashApproachKey, hashApproach);
        params.put(saltKey, salt);
        params.put(failFastKey, failFast);
        params.put(storeIdKey, storeId);
        params.put(providedListingSpaceIdAKey, providedListingSpaceIdA);
        params.put(providedListingSpaceIdBKey, providedListingSpaceIdB);
        params.put(providedListingContentIdAKey, providedListingContentIdA);
        params.put(providedListingContentIdBKey, providedListingContentIdB);
        params.put(targetSpaceIdKey, targetSpaceId);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(outputContentIdKey, outputContentId);
        params.put(reportContentIdKey, reportContentId);

        createServiceOptions();
    }

    private FixityServiceOptions createServiceOptions() {
        return new FixityServiceOptions(params.get(modeKey),
                                        params.get(hashApproachKey),
                                        params.get(saltKey),
                                        params.get(failFastKey),
                                        params.get(storeIdKey),
                                        params.get(providedListingSpaceIdAKey),
                                        params.get(providedListingSpaceIdBKey),
                                        params.get(providedListingContentIdAKey),
                                        params.get(providedListingContentIdBKey),
                                        params.get(targetSpaceIdKey),
                                        params.get(outputSpaceIdKey),
                                        params.get(outputContentIdKey),
                                        params.get(reportContentIdKey));
    }

    @After
    public void tearDown() throws Exception {
        serviceOptions = null;
    }

    @Test
    public void testGetters() throws Exception {
        serviceOptions = createServiceOptions();

        FixityServiceOptions.Mode modeX = serviceOptions.getMode();
        FixityServiceOptions.HashApproach hashApproachX = serviceOptions.getHashApproach();
        String saltX = serviceOptions.getSalt();
        Boolean failFastX = serviceOptions.isFailFast();
        String storeIdX = serviceOptions.getStoreId();
        String providedListingSpaceIdAX = serviceOptions.getProvidedListingSpaceIdA();
        String providedListingSpaceIdBX = serviceOptions.getProvidedListingSpaceIdB();
        String providedListingContentIdAX = serviceOptions.getProvidedListingContentIdA();
        String providedListingContentIdBX = serviceOptions.getProvidedListingContentIdB();
        String targetSpaceIdX = serviceOptions.getTargetSpaceId();
        String outputSpaceIdX = serviceOptions.getOutputSpaceId();
        String outputContentIdX = serviceOptions.getOutputContentId();
        String reportContentIdX = serviceOptions.getReportContentId();

        Assert.assertNotNull(modeX);
        Assert.assertNotNull(hashApproachX);
        Assert.assertNotNull(saltX);
        Assert.assertNotNull(failFastX);
        Assert.assertNotNull(storeIdX);
        Assert.assertNotNull(providedListingSpaceIdAX);
        Assert.assertNotNull(providedListingSpaceIdBX);
        Assert.assertNotNull(providedListingContentIdAX);
        Assert.assertNotNull(providedListingContentIdBX);
        Assert.assertNotNull(targetSpaceIdX);
        Assert.assertNotNull(outputSpaceIdX);
        Assert.assertNotNull(outputContentIdX);
        Assert.assertNotNull(reportContentIdX);

        Assert.assertEquals(mode, modeX.getKey());
        Assert.assertEquals(hashApproach, hashApproachX.name());
        Assert.assertEquals(salt, saltX);
        Assert.assertEquals(failFast, failFastX.toString());
        Assert.assertEquals(storeId, storeIdX);
        Assert.assertEquals(providedListingSpaceIdA, providedListingSpaceIdAX);
        Assert.assertEquals(providedListingSpaceIdB, providedListingSpaceIdBX);
        Assert.assertEquals(providedListingContentIdA,
                            providedListingContentIdAX);
        Assert.assertEquals(providedListingContentIdB,
                            providedListingContentIdBX);
        Assert.assertEquals(targetSpaceId, targetSpaceIdX);
        Assert.assertEquals(outputSpaceId, outputSpaceIdX);
        Assert.assertEquals(outputContentId, outputContentIdX);
        Assert.assertEquals(reportContentId, reportContentIdX);

    }

    @Test
    public void testVerifyFull() {
        serviceOptions = createServiceOptions();
        // FIXME: re-enable validation
        System.out.println("Null service-props checks currently disabled.");
        if (false) {
            // All fields populated != valid
            boolean thrown = false;
            try {
                serviceOptions.verify();
            } catch (Exception e) {
                thrown = true;
                System.out.println("^^^^^^^^^^  Expected Error  ^^^^^^^^^^");
            }
            Assert.assertTrue(thrown);
        }
    }

    @Test
    public void testVerifyNoMode() {
        params.put(modeKey, null);
        serviceOptions = createServiceOptions();

        boolean thrown = false;
        try {
            serviceOptions.verify();
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testVerifyAllInOneListMode() {
        params = new HashMap<String, String>();
        params.put(modeKey, FixityServiceOptions.Mode.ALL_IN_ONE_LIST.getKey());
        params.put(hashApproachKey, hashApproach);
        params.put(saltKey, salt);
        params.put(failFastKey, failFast);
        params.put(storeIdKey, storeId);
        params.put(providedListingSpaceIdAKey, providedListingSpaceIdA);
        params.put(providedListingContentIdAKey, providedListingContentIdA);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(outputContentIdKey, outputContentId);
        params.put(reportContentIdKey, reportContentId);

        serviceOptions = createServiceOptions();
        serviceOptions.verify();
    }

    @Test
    public void testVerifyAllInOneSpaceMode() {
        params = new HashMap<String, String>();
        params.put(modeKey,
                   FixityServiceOptions.Mode.ALL_IN_ONE_SPACE.getKey());
        params.put(hashApproachKey, hashApproach);
        params.put(saltKey, salt);
        params.put(failFastKey, failFast);
        params.put(storeIdKey, storeId);
        params.put(providedListingSpaceIdAKey, providedListingSpaceIdA);
        params.put(providedListingContentIdAKey, providedListingContentIdA);
        params.put(targetSpaceIdKey, targetSpaceId);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(outputContentIdKey, outputContentId);
        params.put(reportContentIdKey, reportContentId);

        serviceOptions = createServiceOptions();
        serviceOptions.verify();
    }


    @Test
    public void testVerifyGenerateListMode() {
        params = new HashMap<String, String>();
        params.put(modeKey, FixityServiceOptions.Mode.GENERATE_LIST.getKey());
        params.put(hashApproachKey, hashApproach);
        params.put(saltKey, salt);
        params.put(storeIdKey, storeId);
        params.put(providedListingSpaceIdAKey, providedListingSpaceIdA);
        params.put(providedListingContentIdAKey, providedListingContentIdA);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(outputContentIdKey, outputContentId);
        params.put(reportContentIdKey, reportContentId);

        serviceOptions = createServiceOptions();
        serviceOptions.verify();
    }

    @Test
    public void testVerifyGenerateSpaceMode() {
        params = new HashMap<String, String>();
        params.put(modeKey, FixityServiceOptions.Mode.GENERATE_SPACE.getKey());
        params.put(hashApproachKey, hashApproach);
        params.put(saltKey, salt);
        params.put(storeIdKey, storeId);
        params.put(targetSpaceIdKey, targetSpaceId);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(outputContentIdKey, outputContentId);
        params.put(reportContentIdKey, reportContentId);

        serviceOptions = createServiceOptions();
        serviceOptions.verify();
    }

    @Test
    public void testVerifyCompareMode() {
        params = new HashMap<String, String>();
        params.put(modeKey, FixityServiceOptions.Mode.COMPARE.getKey());
        params.put(failFastKey, failFast);
        params.put(storeIdKey, storeId);
        params.put(providedListingSpaceIdAKey, providedListingSpaceIdA);
        params.put(providedListingContentIdAKey, providedListingContentIdA);
        params.put(providedListingSpaceIdBKey, providedListingSpaceIdB);
        params.put(providedListingContentIdBKey, providedListingContentIdB);
        params.put(outputSpaceIdKey, outputSpaceId);
        params.put(reportContentIdKey, reportContentId);

        serviceOptions = createServiceOptions();
        serviceOptions.verify();
    }

    @Test
    public void testAutoGenerateMode() {
        params = new HashMap<String, String>();
        boolean auto;
        for (FixityServiceOptions.Mode mode : FixityServiceOptions.Mode
            .values()) {
            params.put(modeKey, mode.getKey());
            serviceOptions = createServiceOptions();

            auto = mode.equals(FixityServiceOptions.Mode.ALL_IN_ONE_SPACE);
            Assert.assertEquals(auto,
                                serviceOptions.needsToAutoGenerateHashListing());
        }
    }

    @Test
    public void testDefaults()
    {
        // hash approach
        params.put(hashApproachKey, null);
        serviceOptions = createServiceOptions();

        Assert.assertNotNull(serviceOptions.getHashApproach());
        Assert.assertEquals(FixityServiceOptions.HashApproach.GENERATED,
                            serviceOptions.getHashApproach());

        // provided listing space id
        params.put(outputSpaceIdKey, "some-output-space-id");
        params.put(providedListingSpaceIdAKey, null);
        serviceOptions = createServiceOptions();

        Assert.assertNotNull(serviceOptions.getProvidedListingSpaceIdA());
        Assert.assertEquals(serviceOptions.getOutputSpaceId(),
                            serviceOptions.getProvidedListingSpaceIdA());

        // provided listing content id
        params.put(providedListingContentIdAKey, null);
        serviceOptions = createServiceOptions();

        Assert.assertNotNull(serviceOptions.getProvidedListingContentIdA());
        Assert.assertEquals(FixityServiceOptions.defaultGenContentId.replace(
            "$TIMESTAMP",
            null + "-" + DateUtil.nowShort()),
                            serviceOptions.getProvidedListingContentIdA());
    }

    @Test
    public void testTimestampDefaults()
    {
        // output content id
        params.put(outputContentIdKey, null);
        params.put(reportContentIdKey, null);

        String now = DateUtil.nowShort();
        String qualifier;

        // all-in-one space
        qualifier = targetSpaceId + "-" + now;
        doTestTimestampDefaults(FixityServiceOptions.Mode.ALL_IN_ONE_SPACE,
                                qualifier);

        // generate space
        doTestTimestampDefaults(FixityServiceOptions.Mode.GENERATE_SPACE,
                                qualifier);

        // all-in-one list
        qualifier = providedListingContentIdA + "-" + now;
        doTestTimestampDefaults(FixityServiceOptions.Mode.ALL_IN_ONE_LIST,
                                qualifier);

        // generate list
        doTestTimestampDefaults(FixityServiceOptions.Mode.GENERATE_LIST,
                                qualifier);

        // compare
        qualifier =
            providedListingContentIdA + "-vs-" + providedListingContentIdB +
                "-" + now;
        doTestTimestampDefaults(FixityServiceOptions.Mode.COMPARE, qualifier);
    }

    private void doTestTimestampDefaults(FixityServiceOptions.Mode mode, String qualifier) {
        params.put(modeKey, mode.name());
        serviceOptions = createServiceOptions();

        Assert.assertNotNull(serviceOptions.getOutputContentId());
        Assert.assertNotNull(serviceOptions.getReportContentId());
        Assert.assertEquals("bitintegrity/fingerprints-" + qualifier + ".csv",
                            serviceOptions.getOutputContentId());
        Assert.assertEquals("bitintegrity/fixity-report-" + qualifier + ".csv",
                            serviceOptions.getReportContentId());
    }

}
