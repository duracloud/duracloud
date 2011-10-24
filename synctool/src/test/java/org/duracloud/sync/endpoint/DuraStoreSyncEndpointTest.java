/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.client.ContentStore;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: 10/24/11
 */
public class DuraStoreSyncEndpointTest {

    private DuraStoreSyncEndpoint endpoint;
    private ContentStore contentStore;
    private String spaceId;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock(ContentStore.class);

        EasyMock
            .expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
            .andReturn(new ArrayList<String>().iterator())
            .anyTimes();

        replayMocks();

        spaceId = "spaceId";
        endpoint = new DuraStoreSyncEndpoint(contentStore, spaceId, false);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testGetContentId() throws Exception {
        File watchDir = new File("a");
        MonitoredFile file = new MonitoredFile(new File("a/b/c", "file.txt"));

        // Get Content ID with a watch dir
        String contentId = endpoint.getContentId(file, watchDir);
        assertEquals("b/c/file.txt", contentId);

        // Get Content ID with now watch dir
        contentId = endpoint.getContentId(file, null);
        assertEquals("file.txt", contentId);
    }

}
