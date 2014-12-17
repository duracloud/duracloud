/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class monitors and logs evnet point events.
 * @author Daniel Bernstein
 *
 */
public class EndPointLogger implements EndPointListener {
    private Logger log = LoggerFactory.getLogger("history-log");
    
    @Override
    public void contentBackedUp(String storeId,
                                String spaceId,
                                String contentId,
                                String backupContentId,
                                String localFilePath) {
        log.info(format(new Object[] {
            SyncResultType.BACKED_UP, storeId, spaceId, contentId, localFilePath,
            "back up copy: " + backupContentId }));
    }

    private String format(Object[] objects) {
        return StringUtils.join(objects, "\t");
    }

    @Override
    public void contentAdded(String storeId,
                             String spaceId,
                             String contentId,
                             String localFilePath) {
        log.info(format(new Object[] {
            SyncResultType.ADDED, storeId, spaceId, contentId, localFilePath}));
    }

    @Override
    public void contentUpdated(String storeId,
                               String spaceId,
                               String contentId,
                               String localFilePath) {
        log.info(format(new Object[] {
            SyncResultType.UPDATED, storeId, spaceId, contentId, localFilePath}));
    }

    @Override
    public void contentDeleted(String storeId,
                               String spaceId,
                               String contentId) {
        log.info(format(new Object[] { 
            SyncResultType.DELETED, storeId, spaceId, contentId}));
    }
    
    @Override
    public void contentUpdateIgnored(String storeId,
                               String spaceId,
                               String contentId,
                               String localFilePath) {
        log.info(format(new Object[] { 
            SyncResultType.UPDATE_IGNORED,
            storeId, spaceId, contentId, localFilePath,
            "local content changed but was not uploaded" }));
    }
}
