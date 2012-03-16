/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.image.status.StatusListener;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 5/31/11
 */
public class ConversionResultProcessorTest {

    private ConversionResultProcessor processor;

    private ContentStore contentStore;
    private StatusListener statusListener;

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore, statusListener);
    }

    @Test
    public void testProcessConversionResultSuccess() throws Exception {
        String errMessage = null;
        doTestProcessConversionResult(errMessage);
    }

    @Test
    public void testProcessConversionResultError() throws Exception {
        String errMessage = "error-message";
        doTestProcessConversionResult(errMessage);
    }

    private void doTestProcessConversionResult(String errMessage)
        throws ContentStoreException {
        contentStore = createMockStore(errMessage != null ? 1 : 0);
        statusListener = createMockListener(errMessage);

        ConversionResult result = newConversionResult(errMessage);

        processor = new ConversionResultProcessor(contentStore,
                                                  statusListener,
                                                  null,
                                                  null,
                                                  null,
                                                  null);

        // this test passes if mock verification succeeds
        processor.processConversionResult(result);
    }


    private ContentStore createMockStore(int errors) throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);

        EasyMock.expect(store.addContent(EasyMock.<String>isNull(),
                                         EasyMock.<String>anyObject(),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.anyLong(),
                                         EasyMock.<String>anyObject(),
                                         EasyMock.<String>isNull(),
                                         EasyMock.<Map<String, String>>isNull()))
            .andReturn(null).times(1 + errors);

        EasyMock.replay(store);
        return store;
    }

    private StatusListener createMockListener(String errMessage) {
        StatusListener listener = EasyMock.createMock("StatusListener",
                                                      StatusListener.class);

        EasyMock.replay(listener);
        return listener;
    }

    private ConversionResult newConversionResult(String errMessage) {
        Date conversionDate = new Date();
        String sourceSpaceId = "source-space-id";
        String destSpaceId = "dest-space-id";
        String contentId = "content-id";
        boolean success = null == errMessage;
        long conversionTime = 1;
        long totalTime = 2;
        long fileSize = 3;
        return new ConversionResult(conversionDate,
                                    sourceSpaceId,
                                    destSpaceId,
                                    contentId,
                                    success,
                                    errMessage,
                                    conversionTime,
                                    totalTime,
                                    fileSize);
    }
}
