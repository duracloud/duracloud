/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.manifest.ContentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class formats content manifests as BagIt.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class BagitManifestFormatter extends ManifestFormatterBase {

    private final Logger log =
        LoggerFactory.getLogger(BagitManifestFormatter.class);

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected String getHeader() {
        return null;
    }

    @Override
    protected String
        getLine(String contentChecksum, String spaceId, String contentId) {
        StringBuilder line = new StringBuilder();
        line.append(contentChecksum);
        line.append("  ");
        line.append(spaceId);
        line.append("/");
        line.append(contentId);

        return line.toString();
    }
}
