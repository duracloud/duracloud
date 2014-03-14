/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.storage.aop.ContentMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.storage.aop.ContentMessage.DELIM;

/**
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class ContentMessageTest {

    private ContentMessage msg;

    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String contentId = "content-id";
    private String username = "username";
    private String action = ContentMessage.ACTION.DELETE.name();
    private String datetime = "Wed, 21 Mar 2012 02:06:21 UTC";


    @Before
    public void setUp() throws Exception {
        msg = new ContentMessage();
        msg.setStoreId(storeId);
        msg.setSpaceId(spaceId);
        msg.setContentId(contentId);
        msg.setUsername(username);
        msg.setAction(action);
        msg.setDatetime(datetime);
    }

    @Test
    public void testConstructor() throws Exception {
        String tsv = msg.asTSV();

        ContentMessage newMsg = new ContentMessage(tsv);
        Assert.assertEquals(msg, newMsg);
    }

    @Test
    public void testAsTSV() throws Exception {
        String tsv = msg.asTSV();
        Assert.assertNotNull(tsv);

        StringBuilder sb = new StringBuilder();
        sb.append(storeId);
        sb.append(DELIM);
        sb.append(spaceId);
        sb.append(DELIM);
        sb.append(contentId);
        sb.append(DELIM);
        sb.append(username);
        sb.append(DELIM);
        sb.append(action);
        sb.append(DELIM);
        sb.append(datetime);

        Assert.assertEquals(sb.toString(), tsv);
    }
}
