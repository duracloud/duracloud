/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.provider;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.model.AclType;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Bill Branan
 *         Date: 3/18/14
 */
@RunWith(EasyMockRunner.class)
public class AuditStorageProviderTest extends EasyMockSupport {

    @TestSubject
    private AuditStorageProvider provider;
    @Mock
    private StorageProvider targetProvider;
    @Mock
    private UserUtil userUtil;
    @Mock
    private TaskQueue taskQueue;

    private String storeId = "store-id";
    private String account = "account";
    private String spaceId = "space-id";
    private String prefix = "prefix";
    private long maxResults = 1;
    private String marker = "marker";
    private String contentId = "content-id";
    private String user = "user";
    private String contentMimeType = "content-mime";
    private long contentSize = 0;
    private String contentChecksum = "content-checksum";

    @Before
    public void setup() {
        provider = new AuditStorageProvider(targetProvider, account, storeId,
                                            userUtil, taskQueue);
    }

    @After
    public void teardown() {
        verifyAll();
    }

    // Test pass-through methods

    @Test
    public void testGetSpaces() {
        EasyMock.expect(targetProvider.getSpaces())
                .andReturn(new ArrayList<String>().iterator());
        replayAll();
        provider.getSpaces();
    }

    @Test
    public void testGetSpaceContents() {
        EasyMock.expect(targetProvider.getSpaceContents(spaceId, prefix))
                .andReturn(new ArrayList<String>().iterator());
        replayAll();
        provider.getSpaceContents(spaceId, prefix);
    }

    @Test
    public void testGetSpaceContentsChunked() {
        EasyMock.expect(
            targetProvider.getSpaceContentsChunked(spaceId, prefix,
                                                   maxResults, marker))
                .andReturn(new ArrayList<String>());
        replayAll();
        provider.getSpaceContentsChunked(spaceId, prefix, maxResults, marker);
    }

    @Test
    public void testGetSpaceProperties() {
        EasyMock.expect(targetProvider.getSpaceProperties(spaceId))
                .andReturn(new HashMap<String, String>());
        replayAll();
        provider.getSpaceProperties(spaceId);
    }

    @Test
    public void testGetSpaceACLs() {
        EasyMock.expect(targetProvider.getSpaceACLs(spaceId))
                .andReturn(new HashMap<String, AclType>());
        replayAll();
        provider.getSpaceACLs(spaceId);
    }

    @Test
    public void testGetContent() {
        EasyMock.expect(targetProvider.getContent(spaceId, contentId))
                .andReturn(null);
        replayAll();
        provider.getContent(spaceId, contentId);
    }

    @Test
    public void testGetContentProperties() {
        EasyMock.expect(targetProvider.getContentProperties(spaceId, contentId))
                .andReturn(new HashMap<String, String>());
        replayAll();
        provider.getContentProperties(spaceId, contentId);
    }

    // Test audit methods

    private Capture<Task> mockAuditCall() throws Exception {
        EasyMock.expect(userUtil.getCurrentUsername()).andReturn(user);
        Capture<Task> auditTaskCapture = new Capture<>();
        taskQueue.put(EasyMock.capture(auditTaskCapture));
        EasyMock.expectLastCall();
        return auditTaskCapture;
    }

    private Map<String, String> verifyAuditTask(Task task, String action) {
        Map<String, String> taskProps = task.getProperties();
        assertEquals(Task.Type.AUDIT, task.getType());
        assertEquals(account, taskProps.get(AuditTask.ACCOUNT_PROP));
        assertEquals(storeId, taskProps.get(AuditTask.STORE_ID_PROP));
        assertEquals(spaceId, taskProps.get(AuditTask.SPACE_ID_PROP));
        assertEquals(user, taskProps.get(AuditTask.USER_ID_PROP));
        assertNotNull(taskProps.get(AuditTask.DATE_TIME_PROP));
        assertEquals(action, taskProps.get(AuditTask.ACTION_PROP));
        return taskProps;
    }

    @Test
    public void testCreateSpace() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        targetProvider.createSpace(spaceId);
        EasyMock.expectLastCall();
        replayAll();
        provider.createSpace(spaceId);

        verifyAuditTask(auditTaskCapture.getValue(),
                        AuditTask.ActionType.CREATE_SPACE.name());
    }

    @Test
    public void testDeleteSpace() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        targetProvider.deleteSpace(spaceId);
        EasyMock.expectLastCall();
        replayAll();
        provider.deleteSpace(spaceId);

        verifyAuditTask(auditTaskCapture.getValue(),
                        AuditTask.ActionType.DELETE_SPACE.name());
    }

    @Test
    public void testSetSpaceACLs() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        targetProvider.setSpaceACLs(spaceId, null);
        EasyMock.expectLastCall();
        replayAll();
        provider.setSpaceACLs(spaceId, null);

        verifyAuditTask(auditTaskCapture.getValue(),
                        AuditTask.ActionType.SET_SPACE_ACLS.name());
    }

    @Test
    public void testAddContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        EasyMock.expect(
            targetProvider.addContent(spaceId, contentId, contentMimeType, null,
                                      contentSize, contentChecksum, null))
                .andReturn("");
        replayAll();
        provider.addContent(spaceId, contentId, contentMimeType, null,
                            contentSize, contentChecksum, null);

        Map<String, String> taskProps =
            verifyAuditTask(auditTaskCapture.getValue(),
                            AuditTask.ActionType.ADD_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
        assertEquals(contentMimeType,
                     taskProps.get(AuditTask.CONTENT_MIMETYPE_PROP));
        assertEquals(String.valueOf(contentSize),
                     taskProps.get(AuditTask.CONTENT_SIZE_PROP));
        assertEquals(contentChecksum,
                     taskProps.get(AuditTask.CONTENT_CHECKSUM_PROP));
    }

    @Test
    public void testCopyContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        EasyMock.expect(
            targetProvider.copyContent(spaceId, contentId, spaceId, contentId))
                .andReturn("");
        replayAll();
        provider.copyContent(spaceId, contentId, spaceId, contentId);

        Map<String, String> taskProps =
            verifyAuditTask(auditTaskCapture.getValue(),
                            AuditTask.ActionType.COPY_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
    }

    @Test
    public void testDeleteContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        targetProvider.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall();
        replayAll();
        provider.deleteContent(spaceId, contentId);

        Map<String, String> taskProps =
            verifyAuditTask(auditTaskCapture.getValue(),
                            AuditTask.ActionType.DELETE_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
    }

    @Test
    public void testSetContentProperties() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();

        targetProvider.setContentProperties(spaceId, contentId, null);
        EasyMock.expectLastCall();
        replayAll();
        provider.setContentProperties(spaceId, contentId, null);

        Map<String, String> taskProps =
            verifyAuditTask(auditTaskCapture.getValue(),
                            AuditTask.ActionType.SET_CONTENT_PROPERTIES.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
    }

}
