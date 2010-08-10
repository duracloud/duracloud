/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.util.CountListener;
import org.duracloud.services.fixity.worker.ServiceWorkload;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashVerifierWorkload implements ServiceWorkload<ContentLocationPair> {
    public HashVerifierWorkload(FixityServiceOptions serviceOptions,
                                ContentStore contentStore) {


    }

    @Override
    public boolean hasNext() {
        // Default method body
        return false;
    }

    @Override
    public ContentLocationPair next() {
        // Default method body
        return null;
    }

    @Override
    public void registerCountListener(CountListener listener) {
        // Default method body

    }
}
