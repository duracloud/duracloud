/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.storage.aop.ContentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

/**
 * This class formats content manifests as tab-separated-values (TSV).
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class TsvManifestFormatter extends ManifestFormatterBase {

    private final Logger log =
        LoggerFactory.getLogger(TsvManifestFormatter.class);

    private static final String HEADER =
        "space-id" + DELIM + "content-id" + DELIM + "MD5";

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    protected String getHeader() {
        return HEADER;
    }

    @Override
    public String getLine(ContentMessage event) {
        StringBuilder line = new StringBuilder();
        line.append(event.getSpaceId());
        line.append(DELIM);
        line.append(event.getContentId());
        line.append(DELIM);
        line.append(event.getContentMd5());

        return line.toString();
    }

}
