/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

import java.text.ParseException;

import org.duracloud.mill.db.model.ManifestItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 * Date: 3/29/12
 */
public class TsvManifestFormatterTest {

    private TsvManifestFormatter formatter;

    private String spaceId = "space-id";
    private String contentId = "content-id";
    private String contentMd5 = "content-md5";

    @Before
    public void setUp() throws Exception {
        formatter = new TsvManifestFormatter();
    }

    @Test
    public void testGetLine() {
        ManifestItem item = new ManifestItem();
        item.setSpaceId(spaceId);
        item.setContentId(contentId);
        item.setContentChecksum(contentMd5);

        String line = formatter.formatLine(item);
        Assert.assertNotNull(line);

        String expected = spaceId + DELIM + contentId + DELIM + contentMd5;
        Assert.assertEquals(expected, line);
    }

    @Test
    public void testGetLineNull() {
        ManifestItem item = new ManifestItem();

        String line = formatter.formatLine(item);
        Assert.assertNotNull(line);

        String nil = null;
        String expected = nil + DELIM + nil + DELIM + nil;
        Assert.assertEquals(expected, line);
    }

    @Test
    public void testParseLine() throws Exception {
        ManifestItem formatItem = new ManifestItem();
        formatItem.setContentChecksum(contentMd5);
        formatItem.setSpaceId(spaceId);
        formatItem.setContentId(contentId);
        String line = formatter.formatLine(formatItem);
        ManifestItem item = formatter.parseLine(line);
        Assert.assertEquals(contentMd5, item.getContentChecksum());
        Assert.assertEquals(contentId, item.getContentId());
        Assert.assertEquals(spaceId, item.getSpaceId());
    }

    @Test
    public void testParseLineFailure() {
        String line = "invalid line";
        try {
            formatter.parseLine(line);
            Assert.fail("parse line method should have failed.");
        } catch (ParseException e) {
            Assert.assertTrue("expected failure", true);
        }
    }

}
