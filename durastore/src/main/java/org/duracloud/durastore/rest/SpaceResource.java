/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.common.model.AclType;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.durastore.error.ResourceNotFoundException;
import org.duracloud.storage.error.InvalidIdException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.SpaceAlreadyExistsException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.IdUtil;
import org.duracloud.storage.util.StorageProviderFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides interaction with spaces
 *
 * @author Bill Branan
 */
public class SpaceResource {

    protected static final Logger log = LoggerFactory.getLogger(SpaceResource.class);

    private StorageProviderFactory storageProviderFactory;

    public SpaceResource(StorageProviderFactory storageProviderFactory) {
        this.storageProviderFactory = storageProviderFactory;
    }

    /**
     * Provides a listing of all spaces for a customer. Open spaces are
     * always included in the list, closed spaces are included based
     * on user authorization.
     *
     * @param storeID
     * @return XML listing of spaces
     */
    public String getSpaces(String storeID) throws ResourceException {

        Element spacesElem = new Element("spaces");

        try {
            StorageProvider storage =
                storageProviderFactory.getStorageProvider(storeID);

            Iterator<String> spaces = storage.getSpaces();
            while (spaces.hasNext()) {
                String spaceID = spaces.next();
                Element spaceElem = new Element("space");
                spaceElem.setAttribute("id", spaceID);
                spacesElem.addContent(spaceElem);
            }
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("Error attempting to build spaces XML",
                                        e);
        }

        Document doc = new Document(spacesElem);
        XMLOutputter xmlConverter = new XMLOutputter();
        return xmlConverter.outputString(doc);
    }

    /**
     * Gets the properties of a space.
     *
     * @param spaceID
     * @param storeID
     * @return Map of space properties
     */
    public Map<String, String> getSpaceProperties(String spaceID, String storeID) throws ResourceException {
        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(storeID);
            return storage.getSpaceProperties(spaceID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("retrieve space properties for", spaceID, e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("retrieve space properties for", spaceID, e);
        }
    }

    /**
     * Gets the ACLs of a space.
     *
     * @param spaceID
     * @param storeID
     * @return Map of space ACLs
     */
    public Map<String, AclType> getSpaceACLs(String spaceID, String storeID) throws ResourceException {
        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(storeID);
            return storage.getSpaceACLs(spaceID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("retrieve space ACLs for", spaceID, e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("retrieve space ACLs for", spaceID, e);
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
    public String getSpaceContents(String spaceID,
                                   String storeID,
                                   String prefix,
                                   long maxResults,
                                   String marker) throws ResourceException {
        Element spaceElem = new Element("space");
        spaceElem.setAttribute("id", spaceID);

        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(storeID);

            List<String> contents = storage.getSpaceContentsChunked(spaceID,
                                                                    prefix,
                                                                    maxResults,
                                                                    marker);
            if (contents != null) {
                for (String contentItem : contents) {
                    Element contentElem = new Element("item");
                    contentElem.setText(contentItem);
                    spaceElem.addContent(contentElem);
                }
            }
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("build space XML for", spaceID, e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
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
     * @param storeID
     */
    public void addSpace(String spaceID, Map<String, AclType> userACLs, String storeID)
        throws ResourceException, InvalidIdException {
        IdUtil.validateSpaceId(spaceID);

        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(storeID);
            storage.createSpace(spaceID);

            waitForSpaceCreation(storage, spaceID);
            updateSpaceACLs(spaceID, userACLs, storeID);
        } catch (SpaceAlreadyExistsException e) {
            throw e;
        } catch (NotFoundException e) {
            throw new InvalidIdException(e.getMessage());
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("add space", spaceID, e);
        }
    }

    private void waitForSpaceCreation(StorageProvider storage, String spaceID) {
        int maxTries = 10;
        int tries = 0;
        long millis = 1500;
        while (tries < maxTries) {
            tries++;
            try {
                storage.getSpaceACLs(spaceID);
                return; // success
            } catch (Exception e) {
                // do nothing
            } finally {
                sleep(millis);
            }
        }

        long elapsed = millis * maxTries;
        String msg = "Space " + spaceID + " !created in " + elapsed + " millis";
        log.error(msg);
        throw new StorageException(msg);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    /**
     * Updates the ACLs of a space.
     *
     * @param spaceID
     * @param spaceACLs
     * @param storeID
     */
    public void updateSpaceACLs(String spaceID,
                                Map<String, AclType> spaceACLs,
                                String storeID) throws ResourceException {
        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(
                storeID);
            if (null != spaceACLs) {
                storage.setSpaceACLs(spaceID, spaceACLs);
            }

        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("update space ACLs for", spaceID, e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("update space ACLs for", spaceID, e);
        }
    }

    /**
     * Deletes a space, removing all included content.
     *
     * @param spaceID
     * @param storeID
     */
    public void deleteSpace(String spaceID, String storeID) throws ResourceException {
        try {
            StorageProvider storage = storageProviderFactory.getStorageProvider(storeID);
            storage.deleteSpace(spaceID);
        } catch (NotFoundException e) {
            throw new ResourceNotFoundException("delete space", spaceID, e);
        } catch (Exception e) {
            storageProviderFactory.expireStorageProvider(storeID);
            throw new ResourceException("delete space", spaceID, e);
        }
    }

}
