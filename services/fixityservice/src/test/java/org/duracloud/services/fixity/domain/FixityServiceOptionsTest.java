/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.domain;

import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class FixityServiceOptionsTest {

    private FixityServiceOptions serviceOptions;

    private String mode = FixityServiceOptions.Mode.GENERATE_LIST.getKey();
    private String hashApproach = FixityServiceOptions.HashApproach
        .SALTED
        .name();
    private String salt = "abc123";
    private Boolean failFast = Boolean.FALSE;
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
        serviceOptions = new FixityServiceOptions(mode,
                                                  hashApproach,
                                                  salt,
                                                  failFast,
                                                  storeId,
                                                  providedListingSpaceIdA,
                                                  providedListingSpaceIdB,
                                                  providedListingContentIdA,
                                                  providedListingContentIdB,
                                                  targetSpaceId,
                                                  outputSpaceId,
                                                  outputContentId,
                                                  reportContentId);
    }

    @After
    public void tearDown() throws Exception {
        serviceOptions = null;
    }

    @Test
    public void testGetters() throws Exception {
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
        Assert.assertEquals(failFast, failFastX);
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
}
