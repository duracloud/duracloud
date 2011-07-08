/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.util;

import org.duracloud.common.util.bulk.ManifestVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * This class overrides the method from common::ManifestVerifier that parses
 * the content-item names and MD5s from a manifest entry.
 * The 'common' version expects:
 * md5 space/name
 * <p/>
 * This version expects:
 * space,name,md5
 *
 * @author Andrew Woods
 *         Date: Aug 12, 2010
 */
public class FixityManifestVerifier extends ManifestVerifier {

    private final Logger log = LoggerFactory.getLogger(FixityManifestVerifier.class);

    public FixityManifestVerifier(File file0, File file1) {
        super(file0, file1);
    }

    protected void addEntry(String line, Map<String, String> entries) {
        if (null == line || line.equals("")) {
            log.warn("Invalid line: " + line);
            return;
        }

        String[] cksumFilenamePair = line.split("\t");
        if (!validEntry(cksumFilenamePair)) {
            throw new RuntimeException("Invalid manifest file: " + line);
        }

        String spaceId = cksumFilenamePair[0].trim();
        String contentId = cksumFilenamePair[1].trim();
        String md5 = cksumFilenamePair[2].trim();

        entries.put(spaceId + "/" + contentId, md5);
    }

    private boolean validEntry(String[] cksumFilenamePair) {
        if (cksumFilenamePair == null) {
            return false;
        }

        if (cksumFilenamePair.length == 3) {
            return true;
        }

        if (cksumFilenamePair.length == 5) {
            return validState(cksumFilenamePair[4]);
        }

        return false;
    }

    private boolean validState(String name) {
        try {
            State.valueOf(name);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    protected String titleOf(String name) {
        int suffixIndex = name.indexOf('/');
        return name.substring(0, suffixIndex);
    }

    protected String fileOf(String name) {
        int suffixIndex = name.indexOf('/');
        return name.substring(suffixIndex + 1, name.length());
    }
}
