/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.FixityServiceOptions;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public abstract class HashWorkloadTestBase {

    protected FixityServiceOptions serviceOptions;
    protected ContentStore contentStore;

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
    }

    protected FixityServiceOptions createServiceOptions(Mode m,
                                                        HashApproach ha) {
        String hashApproachName = ha == null ? null : ha.name();
        return new FixityServiceOptions(m.getKey(),
                                        hashApproachName,
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

    protected abstract ContentStore createContentStore()
        throws ContentStoreException;

}