package org.duracloud.manifest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.ParseException;

import org.duracloud.mill.db.model.ManifestItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExtendedTsvManifestFormatterTest {

    private ExtendedTsvManifestFormatter formatter;

    private final String spaceId = "space-id";
    private final String contentId = "content-id";
    private final String contentMd5 = "content-md5";
    private final String contentMimeType = "text/plain";
    private final String contentSize = "5";


    @Before
    public void setUp() throws Exception {
        formatter = new ExtendedTsvManifestFormatter();
    }

    @Test
    public void testGetLine() {
        ManifestItem item = new ManifestItem();
        item.setSpaceId(spaceId);
        item.setContentId(contentId);
        item.setContentChecksum(contentMd5);
        item.setContentSize(contentSize);
        item.setContentMimetype(contentMimeType);

        String line = formatter.formatLine(item);
        Assert.assertNotNull(line);

        String expected = String.join("\t", spaceId, contentId, contentMd5, contentSize, contentMimeType);
        assertEquals(expected, line);
    }

    @Test
    public void testGetLineNull() {
        ManifestItem item = new ManifestItem();

        String line = formatter.formatLine(item);
        Assert.assertNotNull(line);

        String nil = null;
        String expected = String.join("\t", nil, nil, nil, nil, nil);
        assertEquals(expected, line);
    }

    @Test
    public void testParseLine() throws Exception {
        ManifestItem formatItem = new ManifestItem();
        formatItem.setContentChecksum(contentMd5);
        formatItem.setSpaceId(spaceId);
        formatItem.setContentId(contentId);
        formatItem.setContentSize(contentSize);
        formatItem.setContentMimetype(contentMimeType);

        String line = formatter.formatLine(formatItem);
        ManifestItem item = formatter.parseLine(line);
        assertEquals(contentMd5, item.getContentChecksum());
        assertEquals(contentId, item.getContentId());
        assertEquals(spaceId, item.getSpaceId());
        assertEquals(contentSize, item.getContentSize());
        assertEquals(contentMimeType, item.getContentMimetype());
    }

    @Test
    public void testParseLineFailure() {
        String line = "invalid\tline";
        try {
            formatter.parseLine(line);
            fail("parse line method should have failed.");
        } catch (ParseException e) {
            assertTrue("expected failure", true);
        }
    }

}