/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public class ManifestResource {

    private final Logger log = LoggerFactory.getLogger(ManifestResource.class);

    private ManifestGenerator manifestGenerator;

    public ManifestResource(ManifestGenerator manifestGenerator) {
        this.manifestGenerator = manifestGenerator;
    }

    public InputStream getManifest(String account,
                                   String storeId,
                                   String spaceId,
                                   String fmt)
        throws ManifestArgumentException, ManifestNotFoundException {

        return manifestGenerator.getManifest(account, 
                                             storeId,
                                             spaceId,
                                             validateFormat(fmt));
    }

    private ManifestFormat validateFormat(String format)
        throws ManifestArgumentException {
        // null is default.
        if (null == format) {
            return ManifestFormat.TSV;
        }

        try {
            return ManifestFormat.valueOf(format.toUpperCase());

        } catch (RuntimeException e) {
            StringBuilder err = new StringBuilder("Invalid manifest format: ");
            err.append(format);
            err.append(" Allowable formats are: '");
            for (ManifestFormat f : ManifestFormat.values()) {
                err.append(f.name());
                err.append("', '");
            }
            err.delete(err.length() - 3, err.length());

            log.error(err.toString());
            throw new ManifestArgumentException(err.toString());
        }
    }

 

}
