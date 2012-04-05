/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest;

import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;

import java.io.InputStream;
import java.util.Date;

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
     * This enum defines the supported output manifest formats.
     */
    public static enum FORMAT {
        TSV, BAGIT;
    }

    /**
     * This method generates the manifest for the given args.
     *
     * @param storeId  of manifest items
     * @param spaceId  of manifest items
     * @param format   of manifest (see {@link FORMAT})
     * @param asOfDate of manifest items (format: 'EEE, d MMM yyyy HH:mm:ss z')
     * @return {@link InputStream} of manifest content
     * @throws ManifestArgumentException if format or date are invalid
     * @throws ManifestEmptyException    if no manifest is created
     */
    public InputStream getManifest(String storeId,
                                   String spaceId,
                                   FORMAT format,
                                   Date asOfDate)
        throws ManifestArgumentException, ManifestEmptyException;

}
