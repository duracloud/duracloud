/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.manifest.ContentMessage;
import org.duracloud.mill.db.model.ManifestItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

import java.text.ParseException;

/**
 * @author Andrew Woods
 *         Date: 3/29/12
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
    public void testGetLine() throws Exception {
        ContentMessage event = new ContentMessage();
        event.setSpaceId(spaceId);
        event.setContentId(contentId);
        event.setContentMd5(contentMd5);

        String line = formatter.formatLine(event);
        Assert.assertNotNull(line);

        String expected = spaceId + DELIM + contentId + DELIM + contentMd5;
        Assert.assertEquals(expected, line);
    }

    @Test
    public void testGetLineNull() throws Exception {
        ContentMessage event = new ContentMessage();

        String line = formatter.formatLine(event);
        Assert.assertNotNull(line);

        String nil = null;
        String expected = nil + DELIM + nil + DELIM + nil;
        Assert.assertEquals(expected, line);
    }
    
    @Test
    public void testParseLine() throws Exception {
        String checksum = "checksum-md5";
        String spaceId = "space-id";
        String contentId = "content-id";
        String line = formatter.formatLine(checksum, spaceId, contentId);
        ManifestItem item = formatter.parseLine(line);
        Assert.assertEquals(checksum, item.getContentChecksum());
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
