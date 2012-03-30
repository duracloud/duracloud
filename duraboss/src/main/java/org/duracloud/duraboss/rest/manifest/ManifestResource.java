/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.manifest;

import org.duracloud.common.util.DateUtil;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.manifest.error.ManifestArgumentException;
import org.duracloud.manifest.error.ManifestEmptyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;

import static org.duracloud.manifest.ManifestGenerator.FORMAT;

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

    public InputStream getManifest(String storeId,
                                   String spaceId,
                                   String format,
                                   String date)
        throws ManifestArgumentException, ManifestEmptyException {

        FORMAT fmt = validateFormat(format);
        Date d8 = validateDate(date);

        return manifestGenerator.getManifest(storeId, spaceId, fmt, d8);
    }

    private FORMAT validateFormat(String format)
        throws ManifestArgumentException {
        // null is default.
        if (null == format) {
            return FORMAT.TSV;
        }

        try {
            return FORMAT.valueOf(format.toUpperCase());

        } catch (RuntimeException e) {
            StringBuilder err = new StringBuilder("Invalid manifest format: ");
            err.append(format);
            err.append(" Allowable formats are: '");
            for (ManifestGenerator.FORMAT f : FORMAT.values()) {
                err.append(f.name());
                err.append("', '");
            }
            err.delete(err.length() - 3, err.length());

            log.error(err.toString());
            throw new ManifestArgumentException(err.toString());
        }
    }

    private Date validateDate(String date) throws ManifestArgumentException {
        // null is default.
        if (null == date) {
            return null;
        }

        Exception exception = null;
        for (DateUtil.DateFormat dateFormat : DateUtil.DateFormat.values()) {
            try {
                return DateUtil.convertToDate(date, dateFormat);
            } catch (ParseException e) {
                exception = e;
            }
        }

        StringBuilder err = new StringBuilder("Invalid date format: ");
        err.append(date);
        err.append(" Allowable formats are: ");

        for (DateUtil.DateFormat dateFormat : DateUtil.DateFormat.values()) {
            err.append("'");
            err.append(dateFormat.getPattern());
            err.append("', \n");
        }
        err.delete(err.length() - 3, err.length());
        err.append(".");

        log.error(err.toString());
        throw new ManifestArgumentException(err.toString(), exception);

    }

}
