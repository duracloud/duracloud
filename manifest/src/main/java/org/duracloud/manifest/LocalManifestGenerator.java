/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest;

import org.duracloud.client.ContentStoreManager;

/**
 * The Manifest Generator is responsible for creating content manifests from
 * an existing audit log.
 * Supported output formats include: Tab-Separated-Value & BagIt.
 *
 * @author Andrew Woods
 *         Date: 3/27/12
 */
public interface LocalManifestGenerator extends ManifestGenerator {

    /**
     * This method initializes the Manifest Generator by providing a handle to
     * the content store.
     *
     * @param storeMgr storage manager
     */
    public void initialize(ContentStoreManager storeMgr);

}
