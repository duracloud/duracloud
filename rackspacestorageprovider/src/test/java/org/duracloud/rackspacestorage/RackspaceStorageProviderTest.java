/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesObjectMetaData;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 8/12/11
 */
public class RackspaceStorageProviderTest {

    private FilesClient filesClient;

    @Before
    public void setUp() throws Exception {
        filesClient = EasyMock.createMock("FilesClient", FilesClient.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(filesClient);
    }

    @Test
    public void testCopyContent() throws Exception {
        createFilesClientCopyContent();
        RackspaceStorageProvider provider = new RackspaceStorageProvider(
            filesClient);

        String srcSpaceId = "spaceId";
        String srcContentId = "contentId";
        String destSpaceId = "destSpaceId";
        String destContentId = "destContentId";
        String md5 = provider.copyContent(srcSpaceId,
                                          srcContentId,
                                          destSpaceId,
                                          destContentId);

        Assert.assertNotNull(md5);
        Assert.assertEquals("not-yet-implemented", md5);
    }

    private void createFilesClientCopyContent() throws Exception {
        EasyMock.expect(filesClient.getObjectMetaData(EasyMock.isA(String.class),
                                                      EasyMock.isA(String.class)))
            .andReturn(new FilesObjectMetaData("", "", ""));
        EasyMock.expect(filesClient.containerExists(EasyMock.isA(String.class)))
            .andReturn(true);

        EasyMock.replay(filesClient);
    }
}
