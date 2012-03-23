/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.impl;

import org.duracloud.audit.AuditLogStore;
import org.duracloud.storage.aop.ContentMessage;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.storage.aop.ContentMessage.ACTION.*;

/**
 * @author Andrew Woods
 *         Date: 3/21/12
 */
public class AuditListenerImplTest {

    private AuditListenerImpl listener;
    private AuditLogStore logStore;

    private static final long delay = 200;

    @Before
    public void setUp() throws Exception {
        logStore = EasyMock.createMock("AuditLogStore", AuditLogStore.class);
        listener = new AuditListenerImpl(logStore, delay);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(logStore);
    }

    private void replayMocks() {
        EasyMock.makeThreadSafe(logStore, true);
        EasyMock.replay(logStore);
    }

    @Test
    public void testOnContentEvent() throws Exception {
        String spaceId = "space-id";
        ContentMessage msg0 = createMessage(INGEST, spaceId);
        ContentMessage msg1 = createMessage(COPY, spaceId);

        List<ContentMessage> messagesA = new ArrayList<ContentMessage>();
        messagesA.add(msg0);
        messagesA.add(msg1);

        ContentMessage msg2 = createMessage(DELETE, spaceId);
        ContentMessage msg3 = createMessage(UPDATE, spaceId);

        List<ContentMessage> messagesB = new ArrayList<ContentMessage>();
        messagesB.add(msg2);
        messagesB.add(msg3);

        logStore.write(messagesA);
        EasyMock.expectLastCall();

        logStore.write(messagesB);
        EasyMock.expectLastCall();

        replayMocks();

        listener.onContentEvent(msg0);
        listener.onContentEvent(msg1);

        sleep(delay * 2);

        listener.onContentEvent(msg2);
        listener.onContentEvent(msg3);

        sleep(delay * 2);
    }

    private ContentMessage createMessage(ContentMessage.ACTION action,
                                         String spaceId) {
        ContentMessage msg = new ContentMessage();
        msg.setAction(action.name());
        msg.setSpaceId(spaceId);
        return msg;
    }

    private void sleep(long wait) {
        try {
            Thread.sleep(wait);
        } catch (InterruptedException e) {

        }
    }

    @Test
    public void testWaitToWrite() throws Exception {
        String spaceIdA = "space-id-a";
        String spaceIdB = "space-id-b";

        ContentMessage msg0 = createMessage(INGEST, spaceIdA);
        ContentMessage msg1 = createMessage(COPY, spaceIdA);

        List<ContentMessage> messagesA = new ArrayList<ContentMessage>();
        messagesA.add(msg0);
        messagesA.add(msg1);

        ContentMessage msg2 = createMessage(DELETE, spaceIdB);
        ContentMessage msg3 = createMessage(UPDATE, spaceIdB);

        List<ContentMessage> messagesB = new ArrayList<ContentMessage>();
        messagesB.add(msg2);
        messagesB.add(msg3);

        logStore.write(messagesB);
        EasyMock.expectLastCall();

        replayMocks();

        listener.waitToWrite(spaceIdA, true);
        listener.waitToWrite(spaceIdB, true);

        listener.onContentEvent(msg0);
        listener.onContentEvent(msg1);

        listener.onContentEvent(msg2);
        listener.onContentEvent(msg3);

        listener.waitToWrite(spaceIdB, false);

        sleep(delay * 2);
    }

    @Test
    public void testStop() {
        String spaceId = "space-id";
        ContentMessage msg0 = createMessage(INGEST, spaceId);
        ContentMessage msg1 = createMessage(COPY, spaceId);

        List<ContentMessage> messagesA = new ArrayList<ContentMessage>();
        messagesA.add(msg0);
        messagesA.add(msg1);

        ContentMessage msg2 = createMessage(DELETE, spaceId);
        ContentMessage msg3 = createMessage(UPDATE, spaceId);

        logStore.write(messagesA);
        EasyMock.expectLastCall();

        replayMocks();

        listener.onContentEvent(msg0);
        listener.onContentEvent(msg1);

        sleep(delay * 2);

        listener.stop();

        listener.onContentEvent(msg2);
        listener.onContentEvent(msg3);

        sleep(delay * 2);
    }

}
