/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Aug 26, 2010
 */
public class PostJobWorkerTest {

    private ContentStore contentStore;
    private BulkImageConversionService service;

    @Before
    public void setUp() throws Exception {
        contentStore = createMockContentStore();
        service = createMockService();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        List<String> contents = new ArrayList<String>();
        contents.add("test.jpg");
        EasyMock
            .expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
            .andReturn(contents.iterator())
            .times(1);

        EasyMock
            .expect(contentStore.getContentMetadata(EasyMock.isA(String.class),
                                                    EasyMock.isA(String.class)))
            .andReturn(new HashMap<String, String>())
            .times(1);

        contentStore.setContentMetadata(EasyMock.isA(String.class),
                                        EasyMock.isA(String.class),
                                        EasyMock.isA(Map.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private BulkImageConversionService createMockService() {
        BulkImageConversionService service =
            EasyMock.createMock(BulkImageConversionService.class);

        EasyMock.expect(service.jobComplete()).andReturn(true).times(1);
        
        EasyMock.replay(service);
        return service;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        contentStore = null;

        EasyMock.verify(service);
        service = null;
    }

    @Test
    public void TestWorker() {
        PostJobWorker worker = new PostJobWorker(service,
                                                 contentStore,
                                                 "jpg",
                                                 "spaceId");
        worker.run();
        assertTrue(worker.isComplete());
    }
}
