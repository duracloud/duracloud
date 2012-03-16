/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 9/14/11
 */
public class AddStreamingItemWorkerTest implements StreamingUpdateListener {

    private ContentStore contentStore;
    private String spaceId = "spaceId";
    private String contentId = "contentId";
    private String params = spaceId + ":" + contentId;
    private int updateSuccess = 0;

    @Before
    public void setUp() throws Exception {
        contentStore = createMockContentStore();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Map<String, String> results = new HashMap<String, String>();
        results.put("results", "completed");

        EasyMock
            .expect(contentStore.performTask("add-streaming-item", params))
            .andReturn(SerializationUtil.serializeMap(results))
            .times(1);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        contentStore = null;
    }

    @Test
    public void testAddStreamingItemWorker() throws Exception {
        AddStreamingItemWorker worker =
            new AddStreamingItemWorker(contentStore, spaceId, contentId, this);
        worker.run();
        String result = worker.getAddStreamingItemResult();
        assertNotNull(result);
        assertEquals("completed", result);
        assertEquals(1, updateSuccess);
    }

    @Override
    public void successfulStreamingAddition(String mediaSpaceId,
                                            String mediaContentId) {
        ++updateSuccess;
    }

    @Override
    public void failedStreamingAddition(String mediaSpaceId,
                                        String mediaContentId,
                                        String failureMessage) {
    }

}
