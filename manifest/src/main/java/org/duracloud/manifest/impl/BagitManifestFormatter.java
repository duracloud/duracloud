/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.duracloud.mill.db.model.ManifestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class formats content manifests as BagIt.
 *
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class BagitManifestFormatter extends ManifestFormatterBase {
    private static Pattern LINE_PATTERN = Pattern.compile("(\\w+)\\s+(.*)/(.*)");
    
    private final Logger log =
        LoggerFactory.getLogger(BagitManifestFormatter.class);

    @Override
    protected Logger log() {
        return log;
    }

    @Override
    public String getHeader() {
        return null;
    }

    @Override
    protected String
        formatLine(String contentChecksum, String spaceId, String contentId) {
        StringBuilder line = new StringBuilder();
        line.append(contentChecksum);
        line.append("  ");
        line.append(spaceId);
        line.append("/");
        line.append(contentId);

        return line.toString();
    }
    
    @Override
    public ManifestItem parseLine(String line) throws ParseException {
        Matcher matcher = LINE_PATTERN.matcher(line);
        if (!matcher.find()) {
            throw new ParseException("Line doesn't match bagit format: unable to parse line: ->"
                                     + line + "<-", 0);
        }

        ManifestItem item = new ManifestItem();
        item.setContentChecksum(matcher.group(1));
        item.setSpaceId(matcher.group(2));
        item.setContentId(matcher.group(3));

        return item;

    }
}
