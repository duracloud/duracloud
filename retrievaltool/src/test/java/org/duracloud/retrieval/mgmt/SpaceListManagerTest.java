/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.duracloud.client.ContentStore;
import org.duracloud.retrieval.RetrievalTestBase;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author: Erik Paulsson
 * Date: June 27, 2013
 */
public class SpaceListManagerTest extends RetrievalTestBase {

    @Test
    public void testCountFilesCreated() throws Exception {
        List<String> emptyList = Collections.emptyList();

        ContentStore contentStore = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(contentStore.getStorageProviderType()).andReturn("mock-provider").times(3);
        EasyMock.expect(contentStore.getSpaceContents("space1")).andReturn(emptyList.iterator());
        EasyMock.expect(contentStore.getSpaceContents("space2")).andReturn(emptyList.iterator());
        EasyMock.expect(contentStore.getSpaceContents("space3")).andReturn(emptyList.iterator());
        EasyMock.replay(contentStore);

        List<String> spaces = new ArrayList<String>();
        spaces.add("space1");
        spaces.add("space2");
        spaces.add("space3");

        SpaceListManager mgr = new SpaceListManager(contentStore,
                                                    tempDir,
                                                    spaces,
                                                    true,
                                                    1);
        mgr.run();
        assertTrue(tempDir.exists());
        assertEquals(spaces.size(), tempDir.listFiles().length);
    }
}