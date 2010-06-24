/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.durastore.util.StorageProviderFactory;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProvider.AccessType;
import org.duracloud.storage.util.IdUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides interaction with spaces
 *
 * @author Bill Branan
 */
public class SpaceResource {

    protected static final Logger log = LoggerFactory.getLogger(SpaceResource.class);

    /**
     * Provides a listing of all spaces for a customer. Open spaces are
     * always included in the list, closed spaces are included based
     * on user authorization.
     *
     * @param storeID
     * @return XML listing of spaces
     */
    public static String getSpaces(String storeID)
    throws ResourceException {
        log.debug("Enter");
        Element spacesElem = new Element("spaces");

        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            Iterator<String> spaces = storage.getSpaces();
            while(spaces.hasNext()) {
                String spaceID = spaces.next();
                Element spaceElem = new Element("space");
                spaceElem.setAttribute("id", spaceID);
                spacesElem.addContent(spaceElem);
            }
        } catch (StorageException e) {
            throw new ResourceException("Error attempting to build spaces XML",
                                        e);
        }

        Document doc = new Document(spacesElem);
        XMLOutputter xmlConverter = new XMLOutputter();
        return xmlConverter.outputString(doc);
    }

    /**
     * Gets the metadata of a space.
     *
     * @param spaceID
     * @param storeID
     * @return Map of space metadata
     */
    public static Map<String, String> getSpaceMetadata(String spaceID, String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);
            return storage.getSpaceMetadata(spaceID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("retrieve space metadata for",
                                                spaceID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("retrieve space metadata for",
                                        spaceID,
                                        e);
        }
    }

    /**
     * Gets a listing of the contents of a space.
     *
     * @param spaceID
     * @param storeID
     * @param prefix
     * @param maxResults
     * @param marker
     * @return XML listing of space contents
     */
    public static String getSpaceContents(String spaceID,
                                          String storeID,
                                          String prefix,
                                          long maxResults,
                                          String marker)
    throws ResourceException {
        Element spaceElem = new Element("space");
        spaceElem.setAttribute("id", spaceID);

        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            List<String> contents = storage.getSpaceContentsChunked(spaceID,
                                                                    prefix,
                                                                    maxResults,
                                                                    marker);
            if(contents != null) {
                for(String contentItem : contents) {
                    Element contentElem = new Element("item");
                    contentElem.setText(contentItem);
                    spaceElem.addContent(contentElem);
                }
            }
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("build space XML for",
                                                spaceID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("build space XML for", spaceID, e);
        }

        Document doc = new Document(spaceElem);
        XMLOutputter xmlConverter = new XMLOutputter();
        return xmlConverter.outputString(doc);
    }

    /**
     * Adds a space.
     *
     * @param spaceID
     * @param spaceAccess
     * @param userMetadata
     * @param storeID
     */
    public static void addSpace(String spaceID,
                                String spaceAccess,
                                Map<String, String> userMetadata,
                                String storeID)
    throws ResourceException, InvalidIdException {
        IdUtil.validateSpaceId(spaceID);

        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);
            storage.createSpace(spaceID);
            
            waitForSpaceCreation(storage, spaceID);
            updateSpaceMetadata(spaceID,
                                spaceAccess,
                                userMetadata,
                                storeID);
        } catch (StorageException e) {
            throw new ResourceException("add space", spaceID, e);
        }
    }

    private static void waitForSpaceCreation(StorageProvider storage,
                                             String spaceID) {
        int maxTries = 10;
        int tries = 0;
        long millis = 500;
        while (tries < maxTries) {
            tries++;
            try {
                storage.getSpaceAccess(spaceID);
                return; // success
            } catch (Exception e) {
                sleep(millis);
            }
        }

        long elapsed = millis * maxTries;
        String msg = "Space " + spaceID + " !created in " + elapsed + " millis";
        log.error(msg);
        throw new StorageException(msg);
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    /**
     * Updates the metadata of a space.
     *
     * @param spaceID
     * @param spaceAccess
     * @param userMetadata
     * @param storeID
     */
    public static void updateSpaceMetadata(String spaceID,
                                           String spaceAccess,
                                           Map<String, String> userMetadata,
                                           String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);

            // Update space metadata
            if(userMetadata != null) {
                storage.setSpaceMetadata(spaceID, userMetadata);
            }

            // Set space access if necessary
            if(spaceAccess != null) {
                AccessType access = storage.getSpaceAccess(spaceID);
                AccessType newAccessType = null;
                if(spaceAccess.toUpperCase().equals(AccessType.CLOSED.name())) {
                    newAccessType = AccessType.CLOSED;
                } else if(spaceAccess.toUpperCase().equals(AccessType.OPEN.name())) {
                    newAccessType = AccessType.OPEN;
                }

                if(null == newAccessType) {
                    String error =
                        "Space Access must be set to either OPEN or CLOSED. '" +
                        spaceAccess +"' is not a valid access setting";
                    throw new ResourceException(error);
                } else {
                    if(!access.equals(newAccessType)) {
                        storage.setSpaceAccess(spaceID, newAccessType);
                    }
                }
            }
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("update space metadata for",
                                                spaceID,
                                                e);
        } catch (StorageException e) {
            throw new ResourceException("update space metadata for",
                                        spaceID,
                                        e);
        }
    }

    /**
     * Deletes a space, removing all included content.
     *
     * @param spaceID
     * @param storeID
     */
    public static void deleteSpace(String spaceID, String storeID)
    throws ResourceException {
        try {
            StorageProvider storage =
                StorageProviderFactory.getStorageProvider(storeID);
            storage.deleteSpace(spaceID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("delete space", spaceID, e);
        } catch (StorageException e) {
            throw new ResourceException("delete space", spaceID, e);
        }
    }

}
