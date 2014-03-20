/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.provider;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.provider.StorageProvider;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A StorageProvider decorator class which passes through calls from a target
 * StorageProvider implementation, but captures audit information when
 * changes occur and passes that information to the audit system.
 *
 * @author Bill Branan
 *         Date: 3/14/14
 */
public class AuditStorageProvider implements StorageProvider {

    private StorageProvider target;
    private String account;
    private String storeId;
    private UserUtil userUtil;
    private TaskQueue taskQueue;

    public AuditStorageProvider(StorageProvider target,
                                String account,
                                String storeId,
                                UserUtil userUtil,
                                TaskQueue taskQueue) {
        this.target = target;
        this.account = account;
        this.storeId = storeId;
        this.userUtil = userUtil;
        this.taskQueue = taskQueue;
    }

    private void submitTask(String action,
                            String spaceId,
                            String contentId,
                            String contentChecksum,
                            String contentMimetype,
                            String contentSize,
                            String contentProperties,
                            String spaceACLs) {
        AuditTask task = new AuditTask();
        task.setAction(action);
        task.setUserId(getUserId());
        task.setDateTime(String.valueOf(System.currentTimeMillis()));
        task.setAccount(account);
        task.setStoreId(storeId);
        task.setSpaceId(spaceId);
        task.setSpaceACLs(spaceACLs);
        task.setContentId(contentId);
        task.setContentChecksum(contentChecksum);
        task.setContentMimetype(contentMimetype);
        task.setContentSize(contentSize);
        task.setContentProperties(contentProperties);

        taskQueue.put(task.writeTask());
    }

    private String getUserId() {
        try {
            return userUtil.getCurrentUsername();
        } catch(NoUserLoggedInException e) {
            return AuditTask.NA;
        }
    }

    /*
     * GET methods, these make no changes to the store so are passed through
     */

    @Override
    public Iterator<String> getSpaces() {
        return target.getSpaces();
    }

    @Override
    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        return target.getSpaceContents(spaceId, prefix);
    }

    @Override
    public List<String> getSpaceContentsChunked(String spaceId, String prefix,
                                                long maxResults, String marker) {
        return target.getSpaceContentsChunked(spaceId, prefix,
                                              maxResults, marker);
    }

    @Override
    public Map<String, String> getSpaceProperties(String spaceId) {
        return target.getSpaceProperties(spaceId);
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        return target.getSpaceACLs(spaceId);
    }

    @Override
    public InputStream getContent(String spaceId, String contentId) {
        return target.getContent(spaceId, contentId);
    }

    @Override
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        return target.getContentProperties(spaceId, contentId);
    }

    /*
     * These methods make changes to the state of storage, so audit information
     * needs to be captured for each
     */

    @Override
    public void createSpace(String spaceId) {
        target.createSpace(spaceId);

        String action = AuditTask.ActionType.CREATE_SPACE.name();
        submitTask(action, spaceId, AuditTask.NA, AuditTask.NA,
                   AuditTask.NA, AuditTask.NA, null, null);
    }

    @Override
    public void deleteSpace(String spaceId) {
        target.deleteSpace(spaceId);
        String action = AuditTask.ActionType.DELETE_SPACE.name();
        submitTask(action, spaceId, AuditTask.NA, AuditTask.NA,
                   AuditTask.NA, AuditTask.NA, null, null);
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        target.setSpaceACLs(spaceId, spaceACLs);
        String action = AuditTask.ActionType.SET_SPACE_ACLS.name();
        submitTask(action, spaceId, AuditTask.NA, AuditTask.NA,
                   AuditTask.NA, AuditTask.NA, null, spaceACLs.toString());
    }
    
    @Override
    public String addContent(String spaceId,
                             String contentId,
                             String contentMimeType,
                             Map<String, String> userProperties,
                             long contentSize,
                             String contentChecksum,
                             InputStream content) {
        contentChecksum = target.addContent(spaceId, contentId, contentMimeType,
                                            userProperties, contentSize,
                                            contentChecksum, content);

        String action = AuditTask.ActionType.ADD_CONTENT.name();
        submitTask(action, spaceId, contentId, contentChecksum, contentMimeType,
                   String.valueOf(contentSize), userProperties.toString(), null);
        return contentChecksum;
    }

    @Override
    public String copyContent(String sourceSpaceId, String sourceContentId,
                              String destSpaceId, String destContentId) {
        String contentChecksum =
            target.copyContent(sourceSpaceId, sourceContentId,
                               destSpaceId, destContentId);
        String action = AuditTask.ActionType.COPY_CONTENT.name();
        submitTask(action, destSpaceId, destContentId, contentChecksum,
                   AuditTask.NA, AuditTask.NA, null, null);
        return contentChecksum;
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {
        target.deleteContent(spaceId, contentId);

        String action = AuditTask.ActionType.DELETE_CONTENT.name();
        submitTask(action, spaceId, contentId, AuditTask.NA, AuditTask.NA,
                   AuditTask.NA, null, null);
    }

    @Override
    public void setContentProperties(String spaceId, String contentId,
                                     Map<String, String> contentProperties) {
        target.setContentProperties(spaceId, contentId, contentProperties);

        String action = AuditTask.ActionType.SET_CONTENT_PROPERTIES.name();
        submitTask(action, spaceId, contentId, AuditTask.NA, AuditTask.NA,
                   AuditTask.NA, contentProperties.toString(), null);
    }

}
