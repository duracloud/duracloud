/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.services.duplication.SpaceDuplicator;
import org.duracloud.services.duplication.result.ResultListener;

/**
 * This class implements the SpaceDuplicator contract with the responsibility
 * of requeuing failed duplication attempts and reporting all successful and
 * failed duplications.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public class SpaceDuplicatorReportingImpl implements SpaceDuplicator {

    private SpaceDuplicator spaceDuplicator;

    public SpaceDuplicatorReportingImpl(SpaceDuplicator spaceDuplicator,
                                        ResultListener listener) {
        // Default method body
    }

    @Override
    public void createSpace(String spaceId) {
        // Default method body
    }

    @Override
    public void updateSpace(String spaceId) {
        // Default method body
    }

    @Override
    public void deleteSpace(String spaceId) {
        // Default method body
    }
}
