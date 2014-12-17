/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest;

import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;

import java.io.InputStream;

/**
 * The Manifest Generator is responsible for creating content manifests from
 * an existing audit log.
 * Supported output formats include: Tab-Separated-Value & BagIt.
 *
 * @author Andrew Woods
 *         Date: 3/27/12
 */
public interface ManifestGenerator {

    /**
     * This method generates the manifest for the given args.
     *
     * @param account  of manifest items
     * @param storeId  of manifest items
     * @param spaceId  of manifest items
     * @param format   of manifest
     * @return {@link InputStream} of manifest content
     * @throws ManifestArgumentException if format or date are invalid
     * @throws ManifestNotFoundException    if no manifest is created
     */
    public InputStream getManifest(String account, 
                                   String storeId,
                                   String spaceId,
                                   ManifestFormat format)
        throws ManifestArgumentException, ManifestNotFoundException;

}
