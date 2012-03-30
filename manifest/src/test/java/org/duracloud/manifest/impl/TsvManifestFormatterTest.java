/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.impl;

import org.duracloud.storage.aop.ContentMessage;
import org.duracloud.storage.aop.IngestMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.common.util.bulk.ManifestVerifier.DELIM;

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
        ContentMessage event = new IngestMessage();
        event.setSpaceId(spaceId);
        event.setContentId(contentId);
        event.setContentMd5(contentMd5);

        String line = formatter.getLine(event);
        Assert.assertNotNull(line);

        String expected = spaceId + DELIM + contentId + DELIM + contentMd5;
        Assert.assertEquals(expected, line);
    }

    @Test
    public void testGetLineNull() throws Exception {
        ContentMessage event = new IngestMessage();

        String line = formatter.getLine(event);
        Assert.assertNotNull(line);

        String nil = null;
        String expected = nil + DELIM + nil + DELIM + nil;
        Assert.assertEquals(expected, line);
    }
}
