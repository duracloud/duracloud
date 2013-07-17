/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

/**
 * This interface defines listener methods for monitoring changes in the SyncEndPoint
 * @author Daniel Bernstein
 */
public interface EndPointListener {
    /**
     * 
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param backupContentId
     * @param localFilePath
     */
    void contentBackedUp(String storeId,
                         String spaceId,
                         String contentId,
                         String backupContentId,
                         String localFilePath);
    
    /**
     * 
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param localFilePath
     */
    void contentAdded(String storeId,
                      String spaceId,
                      String contentId,
                      String localFilePath);

    /**
     * 
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param localFilePath 
     */
    void contentUpdated(String storeId,
                        String spaceId,
                        String contentId,
                        String localFilePath);

    /**
     * 
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param localFilePath
     */
    void contentDeleted(String storeId,
                        String spaceId,
                        String contentId,
                        String localFilePath);

    /**
     * 
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param absPath
     */
    void contentUpdateIgnored(String storeId,
                        String spaceId,
                        String contentId,
                        String absPath);
}
