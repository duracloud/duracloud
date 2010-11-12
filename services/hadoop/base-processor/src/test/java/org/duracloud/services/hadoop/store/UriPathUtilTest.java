/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Oct 12, 2010
 */
public class UriPathUtilTest {

       private UriPathUtil pathUtil;

    private String path;
    private String protocol = "s3n://";
    private String uid = "056yyt487ac4801ABe";
    private String spaceId = "space-name";
    private String contentId = "dir0/dir1/content.txt";

    @Before
    public void setUp() throws Exception {
        pathUtil = new UriPathUtil();
        path = protocol + uid + "." + spaceId + "/" + contentId;
    }

    @Test
    public void testGetBucketId() throws Exception {
        String id = pathUtil.getBucketId(path);
        Assert.assertNotNull(id);
        Assert.assertEquals(uid + "." + spaceId, id);
    }

    @Test
    public void testGetSpaceId() throws Exception {
        String id = pathUtil.getSpaceId(path);
        Assert.assertNotNull(id);
        Assert.assertEquals(spaceId, id);
    }

    @Test
    public void testGetContentId() throws Exception {
        String id = pathUtil.getContentId(path);
        Assert.assertNotNull(id);
        Assert.assertEquals(contentId, id);
    }

}
