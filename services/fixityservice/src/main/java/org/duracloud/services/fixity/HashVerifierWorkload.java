/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.common.util.CountListener;
import org.duracloud.services.common.error.ServiceRuntimeException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.worker.ServiceWorkload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashVerifierWorkload implements ServiceWorkload<ContentLocationPair> {

    private final Logger log = LoggerFactory.getLogger(HashVerifierWorkload.class);

    private FixityServiceOptions serviceOptions;
    private Iterator<ContentLocationPair> workload;


    public HashVerifierWorkload(FixityServiceOptions serviceOptions) {
        this.serviceOptions = serviceOptions;
        this.workload = createWorkload();
    }

    private Iterator<ContentLocationPair> createWorkload() {
        List<ContentLocationPair> pairs = new ArrayList<ContentLocationPair>();

        String spaceIdA = serviceOptions.getProvidedListingSpaceIdA();
        String contentIdA = serviceOptions.getProvidedListingContentIdA();
        ContentLocation locA = new ContentLocation(spaceIdA, contentIdA);

        String spaceIdB = null;
        String contentIdB = null;

        FixityServiceOptions.Mode mode = serviceOptions.getMode();
        if (compareFromTwoInputs(mode)) {
            spaceIdB = serviceOptions.getProvidedListingSpaceIdB();
            contentIdB = serviceOptions.getProvidedListingContentIdB();

        } else if (compareFromGeneratedInput(mode)) {
            spaceIdB = serviceOptions.getOutputSpaceId();
            contentIdB = serviceOptions.getOutputContentId();
        }

        if (null == spaceIdB || null == contentIdB) {
            StringBuilder sb = new StringBuilder("Error: either 2nd ");
            sb.append("spaceId or contentId were null");
            sb.append(serviceOptions.toString());
            log.error(sb.toString());

            throw new ServiceRuntimeException(sb.toString());
        }

        ContentLocation locB = new ContentLocation(spaceIdB, contentIdB);
        pairs.add(new ContentLocationPair(locA, locB));

        return pairs.iterator();
    }

    private boolean compareFromTwoInputs(FixityServiceOptions.Mode mode) {
        return mode.equals(FixityServiceOptions.Mode.COMPARE);
    }

    private boolean compareFromGeneratedInput(FixityServiceOptions.Mode mode) {
        return mode.equals(FixityServiceOptions.Mode.ALL_IN_ONE_SPACE) ||
            mode.equals(FixityServiceOptions.Mode.ALL_IN_ONE_LIST);
    }

    @Override
    public boolean hasNext() {
        return workload.hasNext();
    }

    @Override
    public ContentLocationPair next() {
        return workload.next();
    }

    @Override
    public void registerCountListener(CountListener listener) {
        log.info("registerCountListner() ignored.");
    }
}
