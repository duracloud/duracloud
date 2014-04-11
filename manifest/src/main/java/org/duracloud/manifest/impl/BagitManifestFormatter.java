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
    public String getLine(ContentMessage event) {
        StringBuilder line = new StringBuilder();
        line.append(event.getContentMd5());
        line.append("  ");
        line.append(event.getSpaceId());
        line.append("/");
        line.append(event.getContentId());

        return line.toString();
    }
}
