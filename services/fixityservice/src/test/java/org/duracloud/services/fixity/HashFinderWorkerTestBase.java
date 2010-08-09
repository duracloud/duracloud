/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public abstract class HashFinderWorkerTestBase {

    protected Runnable worker;
    protected FixityServiceOptions serviceOptions;
    protected ContentStore contentStore;
    protected ContentLocation workItemLocation;
    protected ServiceResultListener resultListener;

    protected final static String salt = "abc123";
    protected final static String failFast = Boolean.TRUE.toString();
    protected final static String storeId = "1";
    protected final static String providedListingSpaceIdA = "spaceIdA";
    protected final static String providedListingSpaceIdB = "spaceIdB";
    protected final static String providedListingContentIdA = "contentIdA";
    protected final static String providedListingContentIdB = "contentIdB";
    protected final static String targetSpaceId = "targetSpaceId";
    protected final static String outputSpaceId = "outputSpaceId";
    protected final static String outputContentId = "outputContentId";
    protected final static String reportContentId = "reportContentId";

    protected void initialize(Mode mode, HashApproach hashApproach)
        throws ContentStoreException {
        serviceOptions = createServiceOptions(mode, hashApproach);
        contentStore = createContentStore();
        workItemLocation = createWorkItemLocation();
        resultListener = createResultListener();
    }

    protected FixityServiceOptions createServiceOptions(Mode m,
                                                        HashApproach ha) {
        return new FixityServiceOptions(m.getKey(),
                                        ha.name(),
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

    private ContentLocation createWorkItemLocation() {
        return new ContentLocation(providedListingSpaceIdA,
                                   providedListingContentIdA);
    }

    protected String getHash(String text) {
        ChecksumUtil checksumUtil = new ChecksumUtil(MD5);
        return checksumUtil.generateChecksum(getInputStream(text));
    }

    protected InputStream getInputStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    protected abstract ContentStore createContentStore()
        throws ContentStoreException;

    protected abstract ServiceResultListener createResultListener();

}