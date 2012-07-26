/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.services.duplication.ContentDuplicator;
import org.duracloud.services.duplication.Duplicator;
import org.duracloud.services.duplication.SpaceDuplicator;

/**
 * @author: Bill Branan
 * Date: 7/20/12
 */
public class DuplicatorImpl implements Duplicator {

    private SpaceDuplicator spaceDuplicator;
    private ContentDuplicator contentDuplicator;

    public DuplicatorImpl(SpaceDuplicator spaceDuplicator,
                          ContentDuplicator contentDuplicator) {
        this.spaceDuplicator = spaceDuplicator;
        this.contentDuplicator = contentDuplicator;
    }

    @Override
    public String getFromStoreId() {
        return spaceDuplicator.getFromStoreId();
    }

    @Override
    public String getToStoreId() {
        return spaceDuplicator.getToStoreId();
    }

    @Override
    public void createSpace(String spaceId) {
        spaceDuplicator.createSpace(spaceId);
    }

    @Override
    public void updateSpace(String spaceId) {
        spaceDuplicator.updateSpace(spaceId);
    }

    @Override
    public void updateSpaceAcl(String spaceId) {
        spaceDuplicator.updateSpaceAcl(spaceId);
    }

    @Override
    public void deleteSpace(String spaceId) {
        spaceDuplicator.deleteSpace(spaceId);
    }

    @Override
    public String createContent(String spaceId, String contentId) {
        return contentDuplicator.createContent(spaceId, contentId);
    }

    @Override
    public void updateContent(String spaceId, String contentId) {
        contentDuplicator.updateContent(spaceId, contentId);
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {
        contentDuplicator.deleteContent(spaceId, contentId);
    }

    @Override
    public void stop() {
        spaceDuplicator.stop();
        contentDuplicator.stop();
    }

}
