/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;
import static org.duracloud.services.ComputeService.DELIM;

/**
 * @author Andrew Woods
 *         Date: Aug 12, 2010
 */
public class HashVerifierWorkerTest {

    private ContentLocationPair locationPair;
    private ServiceResultListener resultListener;

    private ContentLocation entryLocation = new ContentLocation("space-",
                                                                "content-");

    private ContentLocation locX = new ContentLocation("space-x", "content-x");
    private ContentLocation locY = new ContentLocation("space-y", "content-y");

    private List<String> hashEntriesA;
    private List<String> hashEntriesB; // A and B are same, with different order
    private List<String> hashEntriesC;

    private Map<String, String> expectedStatusValid;
    private Map<String, String> expectedStatusInvalid;

    private File workDir = new File("target/test-hash-verifier");
    private String header = "header";

    private final String newline = System.getProperty("line.separator");
    private static final int NUM_ENTRIES = 25;

    ChecksumUtil checksumUtil = new ChecksumUtil(MD5);


    @Before
    public void setUp() throws Exception {
        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getAbsolutePath(), workDir.mkdir());
        }

        expectedStatusValid = new HashMap<String, String>();
        expectedStatusInvalid = new HashMap<String, String>();

        String valid = "VALID";
        for (int i = 0; i < NUM_ENTRIES; ++i) {
            String spaceId = "space-" + i;

            expectedStatusValid.put(spaceId, valid);
            if (i == 0) {
                expectedStatusInvalid.put(spaceId, "MISMATCH");
            } else if (i % 3 == 0) {
                expectedStatusInvalid.put(spaceId, "MISSING_FROM_1");
            } else {
                expectedStatusInvalid.put(spaceId, valid);
            }
        }

        hashEntriesA = new ArrayList<String>();
        hashEntriesB = new ArrayList<String>();
        hashEntriesC = new ArrayList<String>();

        String data = "data-";
        for (int i = 0; i < NUM_ENTRIES; ++i) {

            hashEntriesA.add(createEntryLine(entryLocation, i, data + i));
            hashEntriesB.add(createEntryLine(entryLocation,
                                             NUM_ENTRIES - 1 - i,
                                             data + (NUM_ENTRIES - 1 - i)));

            if (i % 3 != 0) {
                hashEntriesC.add(createEntryLine(entryLocation,
                                                 i,
                                                 data + i)); // skip every 3rd
            }
            if (i == 0) {
                hashEntriesC.add(createEntryLine(entryLocation,
                                                 i,
                                                 "bad-data")); // bad md5
            }
        }
        
        locationPair = new ContentLocationPair(locX, locY);
        resultListener = createResultListener();
    }

    private String createEntryLine(ContentLocation loc, int i, String data) {
        StringBuilder sb = new StringBuilder(loc.getSpaceId());
        sb.append(i);
        sb.append(DELIM);
        sb.append(loc.getContentId());
        sb.append(i);
        sb.append(DELIM);
        sb.append(getMd5(data));

        return sb.toString();
    }

    private String getMd5(String text) {
        return checksumUtil.generateChecksum(getInputStream(text));
    }

    @Test
    public void testRunValid() throws Exception {
        ContentStore contentStore = createContentStore(hashEntriesA,
                                                       hashEntriesB);
        new HashVerifierWorker(contentStore,
                               locationPair,
                               workDir,
                               resultListener).run();
    }

    @Test
    public void testRunInvalid() throws Exception {
        ContentStore contentStore = createContentStore(hashEntriesA,
                                                       hashEntriesC);
        new HashVerifierWorker(contentStore,
                               locationPair,
                               workDir,
                               resultListener).run();
    }

    private ContentStore createContentStore(List<String> entries0,
                                            List<String> entries1)
        throws ContentStoreException {
        Content content0 = createMockContent(entries0);
        Content content1 = createMockContent(entries1);

        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getContent(locX.getSpaceId(),
                                         locX.getContentId())).andReturn(
            content0);
        EasyMock.expect(store.getContent(locY.getSpaceId(),
                                         locY.getContentId())).andReturn(
            content1);

        EasyMock.replay(store);

        return store;
    }

    private Content createMockContent(List<String> entries) {
        InputStream inputStream = getInputStream(getEntriesAsString(entries));
        Content content = EasyMock.createMock("Content", Content.class);
        EasyMock.expect(content.getStream()).andReturn(inputStream);
        EasyMock.replay(content);
        return content;
    }

    private String getEntriesAsString(List<String> entries) {
        StringBuilder sb = new StringBuilder(header);
        sb.append(newline);
        for (String entry : entries) {
            sb.append(entry);
            sb.append(newline);
        }
        return sb.toString();
    }

    private InputStream getInputStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }

    private ServiceResultListener createResultListener() {
        ServiceResultListener listener = EasyMock.createMock(
            "ServiceResultListener",
            ServiceResultListener.class);
        listener.processServiceResult(HashWorkerMockSupport.matchesResult(
            expectedStatusValid,
            expectedStatusInvalid));
        EasyMock.replay(listener);
        return listener;
    }

}
