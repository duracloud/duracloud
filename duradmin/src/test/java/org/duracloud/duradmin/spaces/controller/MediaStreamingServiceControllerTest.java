/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.test.AbstractTestBase;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Daniel Bernstein
 */
public class MediaStreamingServiceControllerTest extends AbstractTestBase {
    private MediaStreamingTaskController controller;

    @Override
    @Before
    public void setup() {
        super.setup();
        ContentStoreManager contentStoreManager = createMock(ContentStoreManager.class);
        ContentStore store = createMock(ContentStore.class);

        try {
            EasyMock.expect(contentStoreManager.getContentStore(EasyMock.isA(String.class)))
                    .andReturn(store);
            EasyMock.expect(store.performTask(EasyMock.isA(String.class),
                                              EasyMock.isA(String.class)))
                    .andReturn("{\"result\":\"result\"}");
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
        replay();

        controller = new MediaStreamingTaskController(contentStoreManager);
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testEnableHlsStreaming() throws Exception {
        boolean enable = true;
        ModelAndView mav = controller.enableHlsStreaming("testStore", "testSpace", enable);
        Assert.assertEquals(enable, mav.getModelMap().get(MediaStreamingTaskController.STREAMING_ENABLED_KEY));
    }

}
