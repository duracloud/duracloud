/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.duracloud.audit.AuditListener;
import org.duracloud.audit.AuditLogStore;
import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.aop.ContentMessage;
import org.duracloud.storage.aop.IngestMessage;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MD5;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_MODIFIED;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_CONTENT_SIZE;

/**
 * @author Andrew Woods
 *         Date: 3/22/12
 */
public class AuditorImplTest {

    private AuditorImpl auditor;

    private AuditListener listener;
    private AuditLogStore logStore;
    private ContentStoreManager storeManager;

    private ContentStore storeP;
    private ContentStore storeS;

    private static final int NUM_ITEMS = 4;

    @Before
    public void setUp() throws Exception {
        listener = EasyMock.createMock("AuditListener", AuditListener.class);
        logStore = EasyMock.createMock("AuditLogStore", AuditLogStore.class);
        storeManager = EasyMock.createMock("ContentStoreManager",
                                           ContentStoreManager.class);

        storeP = EasyMock.createMock("ContentStoreP", ContentStore.class);
        storeS = EasyMock.createMock("ContentStoreS", ContentStore.class);

        auditor = new AuditorImpl(listener, logStore);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(listener, logStore, storeManager, storeP, storeS);
    }

    private void replayMocks() {
        EasyMock.replay(listener, logStore, storeManager, storeP, storeS);
    }

    @Test
    public void testCreateInitialAuditLogs() throws Exception {
        String spaceId = "space-id";

        // 2 stores
        // 3 spaces each, 2 overlap
        // 4 items in each space

        logStore.initialize(storeP);
        EasyMock.expectLastCall();

        listener.initialize();
        EasyMock.expectLastCall();

        EasyMock.expect(storeManager.getPrimaryContentStore())
                .andReturn(storeP);

        Map<String, ContentStore> storeMap =
            new HashMap<String, ContentStore>();
        storeMap.put("p", storeP);
        storeMap.put("s", storeS);
        EasyMock.expect(storeManager.getContentStores()).andReturn(storeMap);

        List<String> spacesP = new ArrayList<String>();
        List<String> spacesS = new ArrayList<String>();

        final int numSpaces = 3;
        for (int s = 0; s < numSpaces; ++s) {
            if (0 == s) {
                spacesP.add(spaceId + s + "p");
                spacesS.add(spaceId + s + "s");
            } else {
                spacesP.add(spaceId + s);
                spacesS.add(spaceId + s);
            }
        }

        EasyMock.expect(storeP.getSpaces()).andReturn(spacesP);
        EasyMock.expect(storeS.getSpaces()).andReturn(spacesS);

        List<String> logs = new ArrayList<String>();
        logs.add("audit-log.tsv");

        Set<String> allSpaces = new HashSet<String>();
        allSpaces.addAll(spacesP);
        allSpaces.addAll(spacesS);
        for (String space : allSpaces) {
            listener.waitToWrite(space, true);
            EasyMock.expectLastCall();

            EasyMock.expect(logStore.logs(space)).andReturn(logs.iterator());
            logStore.removeLog(logs.get(0));
            EasyMock.expectLastCall();
        }

        int count = 0;
        Map<String, List<String>> spaceContents =
            new HashMap<String, List<String>>();
        for (String space : allSpaces) {
            spaceContents.put(space, getContents(space, count++));
        }

        for (String space : allSpaces) {
            setGetSpaceContentsExpectation(storeP,
                                           spacesP,
                                           space,
                                           spaceContents);
            setGetSpaceContentsExpectation(storeS,
                                           spacesS,
                                           space,
                                           spaceContents);
        }

        for (String space : allSpaces) {
            setGetContentPropertiesExpectation(storeP,
                                               spacesP,
                                               space,
                                               spaceContents);
            setGetContentPropertiesExpectation(storeS,
                                               spacesS,
                                               space,
                                               spaceContents);

            setWriteEventExpectation(storeP,
                                     "p",
                                     spacesP,
                                     space,
                                     spaceContents);
            setWriteEventExpectation(storeS,
                                     "s",
                                     spacesS,
                                     space,
                                     spaceContents);
        }

        for (String space : allSpaces) {
            listener.waitToWrite(space, false);
            EasyMock.expectLastCall();
        }

        replayMocks();

        auditor.initialize(storeManager);
        auditor.createInitialAuditLogs(false);
    }

    private void setGetSpaceContentsExpectation(ContentStore store,
                                                List<String> spaces,
                                                String space,
                                                Map<String, List<String>> spaceContents)
        throws ContentStoreException {
        if (spaces.contains(space)) {
            EasyMock.expect(store.getSpaceContents(space)).andReturn(
                spaceContents.get(space).iterator());
        }
    }

    private void setGetContentPropertiesExpectation(ContentStore store,
                                                    List<String> spaces,
                                                    String space,
                                                    Map<String, List<String>> spaceContents)
        throws ContentStoreException {
        if (spaces.contains(space)) {
            for (String contentId : spaceContents.get(space)) {
                Map<String, String> props = createProps(contentId);
                EasyMock.expect(store.getContentProperties(space, contentId))
                        .andReturn(props);
            }
        }
    }

    private void setWriteEventExpectation(ContentStore store,
                                          String storeId,
                                          List<String> spaces,
                                          String space,
                                          Map<String, List<String>> spaceContents)
        throws ContentStoreException {
        if (spaces.contains(space)) {
            List<ContentMessage> events = new ArrayList<ContentMessage>();
            for (String contentId : spaceContents.get(space)) {
                EasyMock.expect(store.getStoreId()).andReturn(storeId);
                ContentMessage event = createEvent(storeId, space, contentId);
                events.add(event);
            }
            logStore.write(events);
            EasyMock.expectLastCall();
        }
    }

    private ContentMessage createEvent(String storeId,
                                       String spaceId,
                                       String contentId) {
        IngestMessage event = new IngestMessage();
        String username = null;
        String action = ContentMessage.ACTION.INGEST.name();
        String datetime = "date-" + contentId;
        String contentMimeType = "mime-" + contentId;
        String contentMd5 = "md5-" + contentId;
        long contentSize = 10;

        event.setStoreId(storeId);
        event.setSpaceId(spaceId);
        event.setContentId(contentId);
        event.setUsername(username);
        event.setAction(action);
        event.setDatetime(datetime);
        event.setContentMimeType(contentMimeType);
        event.setContentMd5(contentMd5);
        event.setContentSize(contentSize);
        return event;
    }

    private Map<String, String> createProps(String contentId) {
        String contentMimeType = "mime-" + contentId;
        String contentMd5 = "md5-" + contentId;
        long contentSize = 10;
        String creationDate = "date-" + contentId;

        Map<String, String> props = new HashMap<String, String>();
        props.put(PROPERTIES_CONTENT_MODIFIED, creationDate);
        props.put(PROPERTIES_CONTENT_MIMETYPE, contentMimeType);
        props.put(PROPERTIES_CONTENT_MD5, contentMd5);
        props.put(PROPERTIES_CONTENT_SIZE, Long.toString(contentSize));

        return props;
    }

    private List<String> getContents(String space, int i) {
        List<String> contents = new ArrayList<String>();
        for (int x = 0; x < NUM_ITEMS; ++x) {
            contents.add(space + "-content-" + i + "-" + x);
        }
        return contents;
    }

    @Test
    public void testStop() throws Exception {
        listener.stop();
        EasyMock.expectLastCall();

        replayMocks();

        auditor.stop();
    }

    @Test
    public void testGetAuditLogs() throws Exception {
        String spaceId = "space-id";

        List<String> contentIds = new ArrayList<String>();
        final int numIds = 5;
        for (int id = 0; id < numIds; ++id) {
            contentIds.add("audit/audit-log-" + id + ".tsv");
        }

        EasyMock.expect(logStore.logs(spaceId))
                .andReturn(contentIds.iterator());

        replayMocks();

        List<String> logs = auditor.getAuditLogs(spaceId);
        Assert.assertNotNull(logs);
        Assert.assertEquals(numIds, logs.size());
    }

    @Test
    public void testGetAuditLogsEmpty() throws Exception {
        String spaceId = "space-id";

        List<String> contentIds = new ArrayList<String>();
        EasyMock.expect(logStore.logs(spaceId))
                .andReturn(contentIds.iterator());

        replayMocks();

        try {
            auditor.getAuditLogs(spaceId);
            Assert.fail("exception expected");

        } catch (AuditLogNotFoundException e) {
            Assert.assertTrue(e.getMessage().contains(spaceId));
        }
    }
}
