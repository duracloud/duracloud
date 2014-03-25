/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.task;

import org.duracloud.common.queue.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Bill Branan
 *         Date: 3/14/14
 */
public class AuditTaskTest {

    @Test
    public void testAuditTask() {
        String action = AuditTask.ActionType.ADD_CONTENT.name();
        String userId = "user-id";
        String dateTime = String.valueOf(System.currentTimeMillis());
        String account = "account";
        String storeId = "store-id";
        String spaceId = "space-id";
        String contentId = "content-id";
        String contentChecksum = "content-checksum";
        String contentMimetype = "content-mimetype";
        String contentSize = "content-size";
        Map<String,String> contentProperties = new HashMap<>();
        String spaceAcls = "space-acls";
        String storeType = "store-type";

        // Create initial AuditTask
        AuditTask auditTask = new AuditTask();
        auditTask.setAction(action);
        auditTask.setUserId(userId);
        auditTask.setDateTime(dateTime);
        auditTask.setAccount(account);
        auditTask.setStoreId(storeId);
        auditTask.setSpaceId(spaceId);
        auditTask.setContentId(contentId);
        auditTask.setContentChecksum(contentChecksum);
        auditTask.setContentMimetype(contentMimetype);
        auditTask.setContentSize(contentSize);
        auditTask.setContentProperties(contentProperties);
        auditTask.setSpaceACLs(spaceAcls);
        auditTask.setStoreType(storeType);

        // Test writeTask
        Task task = auditTask.writeTask();
        assertEquals(task.getType(), Task.Type.AUDIT);

        Map<String, String> properties = task.getProperties();
        assertEquals(action, properties.get(AuditTask.ACTION_PROP));
        assertEquals(userId, properties.get(AuditTask.USER_ID_PROP));
        assertEquals(dateTime, properties.get(AuditTask.DATE_TIME_PROP));
        assertEquals(account, properties.get(AuditTask.ACCOUNT_PROP));
        assertEquals(storeId, properties.get(AuditTask.STORE_ID_PROP));
        assertEquals(spaceId, properties.get(AuditTask.SPACE_ID_PROP));
        assertEquals(contentId, properties.get(AuditTask.CONTENT_ID_PROP));
        assertEquals(contentChecksum,
                     properties.get(AuditTask.CONTENT_CHECKSUM_PROP));
        assertEquals(contentMimetype,
                     properties.get(AuditTask.CONTENT_MIMETYPE_PROP));
        assertEquals(contentSize,
                     properties.get(AuditTask.CONTENT_SIZE_PROP));
        assertEquals(contentProperties,
                     AuditTask.deserializeContentProperties(
                                           properties.get(AuditTask.CONTENT_PROPERTIES_PROP)));
        assertEquals(spaceAcls, properties.get(AuditTask.SPACE_ACLS_PROP));
        assertEquals(storeType, properties.get(AuditTask.STORE_TYPE_PROP));

        // Test readTask
        AuditTask readAuditTask = new AuditTask();
        readAuditTask.readTask(task);
        assertEquals(auditTask.getAction(), readAuditTask.getAction());
        assertEquals(auditTask.getUserId(), readAuditTask.getUserId());
        assertEquals(auditTask.getDateTime(), readAuditTask.getDateTime());
        assertEquals(auditTask.getAccount(), readAuditTask.getAccount());
        assertEquals(auditTask.getStoreId(), readAuditTask.getStoreId());
        assertEquals(auditTask.getSpaceId(), readAuditTask.getSpaceId());
        assertEquals(auditTask.getContentId(), readAuditTask.getContentId());
        assertEquals(auditTask.getContentChecksum(),
                     readAuditTask.getContentChecksum());
        assertEquals(auditTask.getContentMimetype(),
                     readAuditTask.getContentMimetype());
        assertEquals(auditTask.getContentSize(),
                     readAuditTask.getContentSize());
        assertEquals(auditTask.getContentProperties(),
                     readAuditTask.getContentProperties());
        assertEquals(auditTask.getSpaceACLs(), readAuditTask.getSpaceACLs());
        assertEquals(auditTask.getStoreType(), readAuditTask.getStoreType());
    }

    @Test
    public void testAuditTaskNA() {
        String action = AuditTask.ActionType.ADD_CONTENT.name();

        // Create initial AuditTask
        AuditTask auditTask = new AuditTask();
        auditTask.setAction(action);
        auditTask.setUserId(AuditTask.NA);
        auditTask.setDateTime(AuditTask.NA);
        auditTask.setAccount(AuditTask.NA);
        auditTask.setStoreId(AuditTask.NA);
        auditTask.setSpaceId(AuditTask.NA);
        auditTask.setContentId(AuditTask.NA);
        auditTask.setContentChecksum(AuditTask.NA);
        auditTask.setContentMimetype(AuditTask.NA);
        auditTask.setContentSize(AuditTask.NA);
        auditTask.setContentProperties(null);
        auditTask.setSpaceACLs(null);
        auditTask.setStoreType(AuditTask.NA);

        verifyEmptyAuditTask(auditTask, action);
    }

    private void verifyEmptyAuditTask(AuditTask auditTask, String action) {
        // Test writeTask
        Task task = auditTask.writeTask();
        assertEquals(task.getType(), Task.Type.AUDIT);

        Map<String, String> properties = task.getProperties();
        assertEquals(1, properties.size());
        assertEquals(action, properties.get(AuditTask.ACTION_PROP));

        // Test readTask
        AuditTask readAuditTask = new AuditTask();
        readAuditTask.readTask(task);
        assertEquals(auditTask.getAction(), readAuditTask.getAction());
        assertNull(readAuditTask.getUserId());
        assertNull(readAuditTask.getDateTime());
        assertNull(readAuditTask.getAccount());
        assertNull(readAuditTask.getStoreId());
        assertNull(readAuditTask.getSpaceId());
        assertNull(readAuditTask.getContentId());
        assertNull(readAuditTask.getContentChecksum());
        assertNull(readAuditTask.getContentMimetype());
        assertNull(readAuditTask.getContentSize());
        assertNull(readAuditTask.getContentProperties());
        assertNull(readAuditTask.getSpaceACLs());
        assertNull(readAuditTask.getStoreType());
    }

    @Test
    public void testAuditTaskNulls() {
        String action = AuditTask.ActionType.ADD_CONTENT.name();

        // Create initial AuditTask
        AuditTask auditTask = new AuditTask();
        auditTask.setAction(action);

        verifyEmptyAuditTask(auditTask, action);
    }

    @Test
    public void testSetInvalidActionType() {
        AuditTask auditTask = new AuditTask();
        try {
            auditTask.setAction("unknown-action");
            fail("Exception expected when attempting to set an invalid action");
        } catch(IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testPropertySerialization() {
        Map<String,String> contentProps = new HashMap<>();
        contentProps.put("key1", "value1");
        contentProps.put("key2", "value2");

        String serProps =
            AuditTask.serializeContentProperties(contentProps);
        String serPropsNoWhitespace = serProps.replaceAll("\\s", "");
        assertTrue(serPropsNoWhitespace.contains("\"key1\":\"value1\""));
        assertTrue(serPropsNoWhitespace.contains("\"key2\":\"value2\""));

        Map<String, String> deserProps =
            AuditTask.deserializeContentProperties(serProps);
        assertEquals(contentProps, deserProps);
    }

}
