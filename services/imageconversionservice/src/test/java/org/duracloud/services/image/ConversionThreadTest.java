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
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Andrew Woods
 *         Date: 5/31/11
 */
public class ConversionThreadTest {

    private ConversionThread thread;
    private StatusListener statusListener;

    private ContentStore contentStore;
    private File workDir = new File(System.getProperty("java.io.tmpdir"));
    private String toFormat = "to-format";
    private String colorSpace = "color-space";
    private String sourceSpaceId = "source-space-id";
    private String destSpaceId = "dest-space-id";
    private String outputSpaceId = "output-space-id";
    private String namePrefix = "name-prefix";
    private String nameSuffix = "name-suffix";
    private int threads = 3;

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore, statusListener);
    }

    @Test
    public void testRunDoneWorking() throws Exception {
        contentStore = createMockStore();
        statusListener = createMockListener();

        thread = new ConversionThread(contentStore,
                                      statusListener,
                                      workDir,
                                      toFormat,
                                      colorSpace,
                                      sourceSpaceId,
                                      destSpaceId,
                                      outputSpaceId,
                                      namePrefix,
                                      nameSuffix,
                                      threads);
        thread.run();
    }

    private StatusListener createMockListener() {
        StatusListener listener = EasyMock.createMock("StatusListener",
                                                      StatusListener.class);

        listener.doneWorking();
        EasyMock.expectLastCall();
        
        EasyMock.replay(listener);
        return listener;
    }

    private ContentStore createMockStore() throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);

        EasyMock.expect(store.getSpaceMetadata(destSpaceId)).andReturn(null);

        Iterator<String> contents = new ArrayList<String>().iterator();
        EasyMock.expect(store.getSpaceContents(sourceSpaceId, namePrefix))
            .andReturn(contents);


        EasyMock.replay(store);
        return store;
    }
}
