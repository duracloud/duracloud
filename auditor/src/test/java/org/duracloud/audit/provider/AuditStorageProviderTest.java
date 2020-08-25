/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.audit.logger.ReadLogger;
import org.duracloud.audit.logger.WriteLogger;
import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.model.AclType;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Bill Branan
 * Date: 3/18/14
 */
@RunWith(EasyMockRunner.class)
public class AuditStorageProviderTest extends EasyMockSupport {

    @Mock
    private StorageProvider targetProvider;
    @Mock
    private UserUtil userUtil;
    @Mock
    private TaskQueue taskQueue;
    @Mock
    private ReadLogger readLogger;
    @Mock
    private WriteLogger writeLogger;

    private String storeId = "store-id";
    private String storeType = "store-type";
    private String account = "account";
    private String spaceId = "space-id";
    private String prefix = "prefix";
    private long maxResults = 1;
    private String marker = "marker";
    private String contentId = "content-id";
    private String range = "bytes=0-10";
    private String user = "user";
    private String contentMimeType = "content-mime";
    private long contentSize = 0;
    private String contentChecksum = "content-checksum";
    private String sourceSpaceId = "source-space-id";
    private String sourceContentId = "source-content-id";

    @TestSubject
    private AuditStorageProvider provider =
        new AuditStorageProvider(targetProvider, account, storeId,
                                 storeType, userUtil, taskQueue);

    @Before
    public void setup() {
        provider.setLoggers(readLogger, writeLogger);
    }

    @After
    public void teardown() {
        verifyAll();
    }

    // Test pass-through methods

    @Test
    public void testGetStorageProviderType() throws Exception {
        EasyMock.expect(targetProvider.getStorageProviderType())
                .andReturn(StorageProviderType.AMAZON_S3);
        replayAll();
        assertEquals(StorageProviderType.AMAZON_S3, provider.getStorageProviderType());
    }

    @Test
    public void testGetSpaces() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getSpaces())
                .andReturn(new ArrayList<String>().iterator());
        replayAll();
        provider.getSpaces();

        verifyTaskSkipSpaceId(logCapture.getValue(),
                              AuditTask.ActionType.GET_SPACES.name());
    }

    @Test
    public void testGetSpaceContents() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getSpaceContents(spaceId, prefix))
                .andReturn(new ArrayList<String>().iterator());
        replayAll();
        provider.getSpaceContents(spaceId, prefix);

        verifyTask(logCapture.getValue(),
                   AuditTask.ActionType.GET_SPACE_CONTENTS.name());
    }

    @Test
    public void testGetSpaceContentsChunked() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(
            targetProvider.getSpaceContentsChunked(spaceId, prefix,
                                                   maxResults, marker))
                .andReturn(new ArrayList<String>());
        replayAll();
        provider.getSpaceContentsChunked(spaceId, prefix, maxResults, marker);

        verifyTask(logCapture.getValue(),
                   AuditTask.ActionType.GET_SPACE_CONTENTS_CHUNKED.name());
    }

    @Test
    public void testGetSpaceProperties() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getSpaceProperties(spaceId))
                .andReturn(new HashMap<String, String>());
        replayAll();
        provider.getSpaceProperties(spaceId);

        verifyTask(logCapture.getValue(),
                   AuditTask.ActionType.GET_SPACE_PROPERTIES.name());
    }

    @Test
    public void testGetSpaceACLs() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getSpaceACLs(spaceId))
                .andReturn(new HashMap<String, AclType>());
        replayAll();
        provider.getSpaceACLs(spaceId);

        verifyTask(logCapture.getValue(),
                   AuditTask.ActionType.GET_SPACE_ACLS.name());
    }

    @Test
    public void testGetContent() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getContent(spaceId, contentId))
                .andReturn(null);
        replayAll();
        provider.getContent(spaceId, contentId);

        Map<String, String> taskProps =
            verifyTask(logCapture.getValue(),
                       AuditTask.ActionType.GET_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
    }

    @Test
    public void testGetContentRange() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getContent(spaceId, contentId, range))
                .andReturn(null);
        replayAll();
        provider.getContent(spaceId, contentId, range);

        Map<String, String> taskProps =
            verifyTask(logCapture.getValue(),
                       AuditTask.ActionType.GET_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
        assertEquals(range, taskProps.get(AuditTask.CONTENT_RANGE_PROP));
    }

    @Test
    public void testGetContentProperties() throws Exception {
        Capture<Task> logCapture = mockReadLogCall();

        EasyMock.expect(targetProvider.getContentProperties(spaceId, contentId))
                .andReturn(new HashMap<String, String>());
        replayAll();
        provider.getContentProperties(spaceId, contentId);

        Map<String, String> taskProps =
            verifyTask(logCapture.getValue(),
                       AuditTask.ActionType.GET_CONTENT_PROPERTIES.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
    }

    // Test audit methods

    private Capture<Task> mockAuditCall() {
        Capture<Task> auditTaskCapture = Capture.newInstance(CaptureType.FIRST);
        taskQueue.put(EasyMock.capture(auditTaskCapture));
        EasyMock.expectLastCall().once();
        return auditTaskCapture;
    }

    private Capture<Task> mockReadLogCall() throws Exception {
        EasyMock.expect(userUtil.getCurrentUsername()).andReturn(user);
        Capture<Task> logCapture = Capture.newInstance(CaptureType.FIRST);
        readLogger.log(EasyMock.capture(logCapture));
        EasyMock.expectLastCall().once();
        return logCapture;
    }

    private Capture<Task> mockWriteLogCall() throws Exception {
        EasyMock.expect(userUtil.getCurrentUsername()).andReturn(user);
        Capture<Task> logCapture = Capture.newInstance(CaptureType.FIRST);
        writeLogger.log(EasyMock.capture(logCapture));
        EasyMock.expectLastCall().once();
        return logCapture;
    }

    private Map<String, String> verifyTaskSkipSpaceId(Task task, String action) {
        Map<String, String> taskProps = task.getProperties();
        assertEquals(Task.Type.AUDIT, task.getType());
        assertEquals(account, taskProps.get(AuditTask.ACCOUNT_PROP));
        assertEquals(storeId, taskProps.get(AuditTask.STORE_ID_PROP));
        assertEquals(user, taskProps.get(AuditTask.USER_ID_PROP));
        assertNotNull(taskProps.get(AuditTask.DATE_TIME_PROP));
        assertEquals(action, taskProps.get(AuditTask.ACTION_PROP));
        return taskProps;
    }

    private Map<String, String> verifyTask(Task task, String action) {
        Map<String, String> taskProps = verifyTaskSkipSpaceId(task, action);
        assertEquals(spaceId, taskProps.get(AuditTask.SPACE_ID_PROP));
        return taskProps;
    }

    @Test
    public void testCreateSpace() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();

        targetProvider.createSpace(spaceId);
        EasyMock.expectLastCall().once();
        replayAll();
        provider.createSpace(spaceId);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        verifyTask(auditTask, AuditTask.ActionType.CREATE_SPACE.name());
    }

    @Test
    public void testDeleteSpace() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();

        targetProvider.deleteSpace(spaceId);
        EasyMock.expectLastCall().once();
        replayAll();
        provider.deleteSpace(spaceId);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        verifyTask(auditTask, AuditTask.ActionType.DELETE_SPACE.name());
    }

    @Test
    public void testSetSpaceACLs() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();
        Map<String, AclType> spaceAcls = new HashMap<>();
        spaceAcls.put(user, AclType.WRITE);

        targetProvider.setSpaceACLs(spaceId, spaceAcls);
        EasyMock.expectLastCall().once();
        replayAll();
        provider.setSpaceACLs(spaceId, spaceAcls);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        Map<String, String> taskProps =
            verifyTask(auditTask, AuditTask.ActionType.SET_SPACE_ACLS.name());
        String spaceAclProp = taskProps.get(AuditTask.SPACE_ACLS_PROP);
        assertNotNull(spaceAclProp);
        assertTrue(spaceAclProp.contains(user));
        assertTrue(spaceAclProp.contains(AclType.WRITE.name()));
    }

    @Test
    public void testAddContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();

        Map<String, String> contentProps = new HashMap<>();
        String propName = "prop-name";
        String propValue = "prop-value";
        contentProps.put(propName, propValue);

        EasyMock.expect(
            targetProvider.addContent(spaceId, contentId, contentMimeType,
                                      contentProps, contentSize,
                                      contentChecksum, null))
                .andReturn(contentChecksum);
        replayAll();
        provider.addContent(spaceId, contentId, contentMimeType, contentProps,
                            contentSize, contentChecksum, null);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        Map<String, String> taskProps =
            verifyTask(auditTask, AuditTask.ActionType.ADD_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
        assertEquals(contentMimeType,
                     taskProps.get(AuditTask.CONTENT_MIMETYPE_PROP));
        assertEquals(String.valueOf(contentSize),
                     taskProps.get(AuditTask.CONTENT_SIZE_PROP));
        assertEquals(contentChecksum,
                     taskProps.get(AuditTask.CONTENT_CHECKSUM_PROP));
        String contentPropsProp =
            taskProps.get(AuditTask.CONTENT_PROPERTIES_PROP);
        assertNotNull(contentPropsProp);
        assertTrue(contentPropsProp.contains(propName));
        assertTrue(contentPropsProp.contains(propValue));
    }

    @Test
    public void testCopyContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();

        EasyMock.expect(targetProvider.copyContent(sourceSpaceId,
                                                   sourceContentId,
                                                   spaceId,
                                                   contentId))
                .andReturn("");

        Map<String, String> props = new HashMap<>();
        props.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE, contentMimeType);
        props.put(StorageProvider.PROPERTIES_CONTENT_SIZE, contentSize + "");

        EasyMock.expect(targetProvider.getContentProperties(sourceSpaceId, sourceContentId))
                .andReturn(props);

        replayAll();
        provider.copyContent(sourceSpaceId, sourceContentId, spaceId, contentId);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        Map<String, String> taskProps =
            verifyTask(auditTask, AuditTask.ActionType.COPY_CONTENT.name());
        assertEquals(sourceSpaceId,
                     taskProps.get(AuditTask.SOURCE_SPACE_ID_PROP));
        assertEquals(sourceContentId,
                     taskProps.get(AuditTask.SOURCE_CONTENT_ID_PROP));
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));

        assertNotNull(taskProps.get(AuditTask.CONTENT_PROPERTIES_PROP));
        assertEquals(contentSize + "", taskProps.get(AuditTask.CONTENT_SIZE_PROP));
        assertEquals(contentMimeType, taskProps.get(AuditTask.CONTENT_MIMETYPE_PROP));

    }

    @Test
    public void testDeleteContent() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();
        Map<String, String> props = new HashMap<>();
        props.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE, contentMimeType);
        props.put(StorageProvider.PROPERTIES_CONTENT_SIZE, contentSize + "");
        props.put(StorageProvider.PROPERTIES_CONTENT_CHECKSUM, contentChecksum);

        EasyMock.expect(targetProvider.getContentProperties(spaceId, contentId))
                .andReturn(props);

        targetProvider.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall().once();
        replayAll();
        provider.deleteContent(spaceId, contentId);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        Map<String, String> taskProps =
            verifyTask(auditTask, AuditTask.ActionType.DELETE_CONTENT.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
        assertEquals(contentSize, Long.parseLong(taskProps.get(AuditTask.CONTENT_SIZE_PROP)));
        assertEquals(contentMimeType, taskProps.get(AuditTask.CONTENT_MIMETYPE_PROP));
        assertEquals(contentChecksum, taskProps.get(AuditTask.CONTENT_CHECKSUM_PROP));

    }

    @Test
    public void testSetContentProperties() throws Exception {
        Capture<Task> auditTaskCapture = mockAuditCall();
        Capture<Task> logCapture = mockWriteLogCall();

        Map<String, String> contentProps = new HashMap<>();
        String propName = "prop-name";
        String propValue = "prop-value";
        contentProps.put(propName, propValue);

        targetProvider.setContentProperties(spaceId, contentId, contentProps);
        EasyMock.expectLastCall().once();
        replayAll();
        provider.setContentProperties(spaceId, contentId, contentProps);

        Task auditTask = auditTaskCapture.getValue();
        assertEquals(auditTask, logCapture.getValue());
        Map<String, String> taskProps =
            verifyTask(auditTask,
                       AuditTask.ActionType.SET_CONTENT_PROPERTIES.name());
        assertEquals(contentId, taskProps.get(AuditTask.CONTENT_ID_PROP));
        String contentPropsProp =
            taskProps.get(AuditTask.CONTENT_PROPERTIES_PROP);
        assertNotNull(contentPropsProp);
        assertTrue(contentPropsProp.contains(propName));
        assertTrue(contentPropsProp.contains(propValue));
    }

}
