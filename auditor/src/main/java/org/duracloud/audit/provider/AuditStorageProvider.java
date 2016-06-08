/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.provider;

import org.duracloud.audit.logger.ReadLogger;
import org.duracloud.audit.logger.WriteLogger;
import org.duracloud.audit.task.AuditTask;
import org.duracloud.common.error.NoUserLoggedInException;
import org.duracloud.common.model.AclType;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.util.UserUtil;
import org.duracloud.storage.domain.StorageProviderType;
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
    private String storeType;
    private UserUtil userUtil;
    private TaskQueue taskQueue;
    private ReadLogger readLogger;
    private WriteLogger writeLogger;

    public AuditStorageProvider(StorageProvider target,
                                String account,
                                String storeId,
                                String storeType,
                                UserUtil userUtil,
                                TaskQueue taskQueue) {
        this.target = target;
        this.account = account;
        this.storeId = storeId;
        this.storeType = storeType;
        this.userUtil = userUtil;
        this.taskQueue = taskQueue;

        this.readLogger = new ReadLogger();
        this.writeLogger = new WriteLogger();
    }

    /*
     * Intended to be used for testing
     */
    protected void setLoggers(ReadLogger readLogger, WriteLogger writeLogger) {
        this.readLogger = readLogger;
        this.writeLogger = writeLogger;
    }

    /*
     * Handles write tasks. Write tasks are passed to the task queue and logged.
     */
    private void submitWriteTask(String action,
                                 String spaceId,
                                 String contentId,
                                 String contentChecksum,
                                 String contentMimetype,
                                 String contentSize,
                                 Map<String,String> contentProperties,
                                 String spaceACLs,
                                 String sourceSpaceId,
                                 String sourceContentId) {
        AuditTask task = new AuditTask();
        task.setAction(action);
        task.setUserId(getUserId());
        task.setDateTime(String.valueOf(System.currentTimeMillis()));
        task.setAccount(account);
        task.setStoreId(storeId);
        task.setStoreType(storeType);
        task.setSpaceId(spaceId);
        task.setSpaceACLs(spaceACLs);
        task.setContentId(contentId);
        task.setContentChecksum(contentChecksum);
        task.setContentMimetype(contentMimetype);
        task.setContentSize(contentSize);
        task.setContentProperties(contentProperties);
        task.setSourceSpaceId(sourceSpaceId);
        task.setSourceContentId(sourceContentId);

        Task writeTask = task.writeTask();
        taskQueue.put(writeTask);
        writeLogger.log(writeTask);
    }

    /*
     * Handles read tasks. Read tasks are only logged (no task queue).
     */
    private void submitReadTask(String action,
                                String spaceId,
                                String contentId) {
        AuditTask task = new AuditTask();
        task.setAction(action);
        task.setUserId(getUserId());
        task.setDateTime(String.valueOf(System.currentTimeMillis()));
        task.setAccount(account);
        task.setStoreId(storeId);
        task.setStoreType(storeType);
        task.setSpaceId(spaceId);
        task.setContentId(contentId);

        readLogger.log(task.writeTask());
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
    public StorageProviderType getStorageProviderType() {
        return target.getStorageProviderType();
    }

    @Override
    public Iterator<String> getSpaces() {
        Iterator<String> spaces = target.getSpaces();

        String action = AuditTask.ActionType.GET_SPACES.name();
        submitReadTask(action, AuditTask.NA, AuditTask.NA);
        return spaces;
    }

    @Override
    public Iterator<String> getSpaceContents(String spaceId, String prefix) {
        Iterator<String> spaceContents =
            target.getSpaceContents(spaceId, prefix);

        String action = AuditTask.ActionType.GET_SPACE_CONTENTS.name();
        submitReadTask(action, spaceId, AuditTask.NA);
        return spaceContents;
    }

    @Override
    public List<String> getSpaceContentsChunked(String spaceId, String prefix,
                                                long maxResults, String marker) {
        List<String> spaceContents =
            target.getSpaceContentsChunked(spaceId, prefix, maxResults, marker);

        String action = AuditTask.ActionType.GET_SPACE_CONTENTS_CHUNKED.name();
        submitReadTask(action, spaceId, AuditTask.NA);
        return  spaceContents;
    }

    @Override
    public Map<String, String> getSpaceProperties(String spaceId) {
        Map<String, String> spaceProps = target.getSpaceProperties(spaceId);

        String action = AuditTask.ActionType.GET_SPACE_PROPERTIES.name();
        submitReadTask(action, spaceId, AuditTask.NA);
        return spaceProps;
    }

    @Override
    public Map<String, AclType> getSpaceACLs(String spaceId) {
        Map<String, AclType> spaceAcls = target.getSpaceACLs(spaceId);

        String action = AuditTask.ActionType.GET_SPACE_ACLS.name();
        submitReadTask(action, spaceId, AuditTask.NA);
        return spaceAcls;
    }

    @Override
    public InputStream getContent(String spaceId, String contentId) {
        InputStream content = target.getContent(spaceId, contentId);

        String action = AuditTask.ActionType.GET_CONTENT.name();
        submitReadTask(action, spaceId, contentId);
        return content;
    }

    @Override
    public Map<String, String> getContentProperties(String spaceId,
                                                    String contentId) {
        Map<String, String> contentProps =
            target.getContentProperties(spaceId, contentId);

        String action = AuditTask.ActionType.GET_CONTENT_PROPERTIES.name();
        submitReadTask(action, spaceId, contentId);
        return contentProps;
    }

    /*
     * These methods make changes to the state of storage, so audit information
     * needs to be captured for each
     */

    @Override
    public void createSpace(String spaceId) {
        target.createSpace(spaceId);

        String action = AuditTask.ActionType.CREATE_SPACE.name();
        submitWriteTask(action, spaceId, AuditTask.NA, AuditTask.NA,
                        AuditTask.NA, AuditTask.NA, null, null, AuditTask.NA,
                        AuditTask.NA);
    }

    @Override
    public void deleteSpace(String spaceId) {
        target.deleteSpace(spaceId);

        String action = AuditTask.ActionType.DELETE_SPACE.name();
        submitWriteTask(action, spaceId, AuditTask.NA, AuditTask.NA,
                        AuditTask.NA, AuditTask.NA, null, null, AuditTask.NA,
                        AuditTask.NA);
    }

    @Override
    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        target.setSpaceACLs(spaceId, spaceACLs);

        String action = AuditTask.ActionType.SET_SPACE_ACLS.name();
        submitWriteTask(action,
                        spaceId,
                        AuditTask.NA,
                        AuditTask.NA,
                        AuditTask.NA,
                        AuditTask.NA,
                        null,
                        spaceACLs == null ? null : spaceACLs.toString(),
                        AuditTask.NA,
                        AuditTask.NA);
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
        submitWriteTask(action, spaceId, contentId, contentChecksum,
                        contentMimeType, String.valueOf(contentSize),
                        userProperties, null, AuditTask.NA, AuditTask.NA);
        return contentChecksum;
    }

    @Override
    public String copyContent(String sourceSpaceId, String sourceContentId,
                              String destSpaceId, String destContentId) {
        String contentChecksum =
            target.copyContent(sourceSpaceId, sourceContentId,
                               destSpaceId, destContentId);

        Map<String,String> props = target.getContentProperties(sourceSpaceId, sourceContentId);
        String contentMimetype = props.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
        String contentSize = props.get(StorageProvider.PROPERTIES_CONTENT_SIZE);
        String action = AuditTask.ActionType.COPY_CONTENT.name();
        submitWriteTask(action, destSpaceId, destContentId, contentChecksum,
                        contentMimetype, contentSize, props, null, sourceSpaceId,
                        sourceContentId);
        return contentChecksum;
    }

    @Override
    public void deleteContent(String spaceId, String contentId) {

        Map<String,String> props = target.getContentProperties(spaceId, contentId);
        String contentMimetype = props.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
        String contentSize = props.get(StorageProvider.PROPERTIES_CONTENT_SIZE);
        String contentChecksum = props.get(StorageProvider.PROPERTIES_CONTENT_CHECKSUM);
        target.deleteContent(spaceId, contentId);
        String action = AuditTask.ActionType.DELETE_CONTENT.name();
        submitWriteTask(action, spaceId, contentId, contentChecksum, contentMimetype,
                        contentSize, null, null, AuditTask.NA, AuditTask.NA);
    }

    @Override
    public void setContentProperties(String spaceId, String contentId,
                                     Map<String, String> contentProperties) {
        target.setContentProperties(spaceId, contentId, contentProperties);

        String action = AuditTask.ActionType.SET_CONTENT_PROPERTIES.name();
        submitWriteTask(action, spaceId, contentId, AuditTask.NA, AuditTask.NA,
                        AuditTask.NA, contentProperties, null, AuditTask.NA,
                        AuditTask.NA);
    }

}
