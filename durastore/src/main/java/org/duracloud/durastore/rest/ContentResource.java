/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.error.ResourceException;
import org.duracloud.storage.error.InvalidIdException;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Aug 19, 2010
 */
public interface ContentResource {
    
    InputStream getContent(String spaceID, String contentID, String storeID)
        throws ResourceException;

    Map<String, String> getContentMetadata(String spaceID,
                                           String contentID,
                                           String storeID)
        throws ResourceException;

    void updateContentMetadata(String spaceID,
                               String contentID,
                               String contentMimeType,
                               Map<String, String> userMetadata,
                               String storeID) throws ResourceException;

    String addContent(String spaceID,
                      String contentID,
                      InputStream content,
                      String contentMimeType,
                      int contentSize,
                      String checksum,
                      String storeID)
        throws ResourceException, InvalidIdException;

    void deleteContent(String spaceID, String contentID, String storeID)
        throws ResourceException;
}
