/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import static org.duracloud.common.util.bulk.ManifestVerifier.*;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.duracloud.mill.db.model.ManifestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class formats content manifests as tab-separated-values (TSV).
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class TsvManifestFormatter extends ManifestFormatterBase {

    private final Logger log =
        LoggerFactory.getLogger(TsvManifestFormatter.class);

    private static Pattern LINE_PATTERN = Pattern.compile("(.+)"+DELIM+"(.+)"+DELIM+"(.+)");

    private static final String HEADER =
        "space-id" + DELIM + "content-id" + DELIM + "MD5";

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    protected String
        formatLine(String contentMd5, String spaceId, String contentId) {
        StringBuilder line = new StringBuilder();
        line.append(spaceId);
        line.append(DELIM);
        line.append(contentId);
        line.append(DELIM);
        line.append(contentMd5);
        return line.toString();    
    }
    

    @Override
    public ManifestItem parseLine(String line) throws ParseException {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new ParseException("Line doesn't match tsv format: unable to parse line: ->"
                                     + line + "<-", 0);
        }

        ManifestItem item = new ManifestItem();
        item.setSpaceId(matcher.group(1));
        item.setContentId(matcher.group(2));
        item.setContentChecksum(matcher.group(3));

        return item;

    }

}
