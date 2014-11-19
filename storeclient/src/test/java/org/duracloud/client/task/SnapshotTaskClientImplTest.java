/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.task;

import org.duracloud.client.ContentStore;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.dto.SnapshotContentItem;
import org.duracloud.snapshot.dto.SnapshotStatus;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.task.CleanupSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.CompleteSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.CreateSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.GetRestoreTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotListTaskResult;
import org.duracloud.snapshot.dto.task.GetSnapshotTaskResult;
import org.duracloud.snapshot.dto.task.RestoreSnapshotTaskResult;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Bill Branan
 *         Date: 8/8/14
 */
public class SnapshotTaskClientImplTest {

    private SnapshotTaskClientImpl taskClient;
    private ContentStore contentStore;

    private String spaceId = "space-id";
    private String description = "desc";
    private String userEmail = "user-email";
    private String snapshotId = "snapshot-id";
    private SnapshotStatus snapshotStatus = SnapshotStatus.INITIALIZED;
    private Date snapshotDate = new Date(System.currentTimeMillis());
    private String host = "snapshot-host";
    private int port = 8080;
    private String storeId = "0";
    private int contentExpirationDays = 42;
    private String restoreId = "restore-id";
    private RestoreStatus restoreStatus = RestoreStatus.WAITING_FOR_DPN;
    private String contentId = "content-id";
    private String propName = "prop-name";
    private String propValue = "prop-value";
    private String completionResult = "result";

    @Before
    public void setup() {
        contentStore = EasyMock.createMock(ContentStore.class);
        taskClient = new SnapshotTaskClientImpl(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @After
    public void teardown() {
        EasyMock.verify(contentStore);
    }

    private void setupMock(String taskName, String preparedResult)
        throws Exception {
        EasyMock.expect(contentStore.performTask(EasyMock.eq(taskName),
                                                 EasyMock.isA(String.class)))
                .andReturn(preparedResult);
    }

    @Test
    public void testCreateSnapshot() throws Exception {
        String taskName = SnapshotConstants.CREATE_SNAPSHOT_TASK_NAME;

        CreateSnapshotTaskResult preparedResult = new CreateSnapshotTaskResult();
        preparedResult.setSnapshotId(snapshotId);
        preparedResult.setStatus(snapshotStatus);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        CreateSnapshotTaskResult result =
            taskClient.createSnapshot(spaceId, description, userEmail);
        assertThat(snapshotId, equalTo(result.getSnapshotId()));
    }

    @Test
    public void testGetSnapshot() throws Exception {
        String taskName = SnapshotConstants.GET_SNAPSHOT_TASK_NAME;

        GetSnapshotTaskResult preparedResult = new GetSnapshotTaskResult();
        preparedResult.setSnapshotId(snapshotId);
        preparedResult.setDescription(description);
        preparedResult.setSnapshotDate(snapshotDate);
        preparedResult.setSourceHost(host);
        preparedResult.setSourceSpaceId(spaceId);
        preparedResult.setSourceStoreId(storeId);
        preparedResult.setStatus(snapshotStatus);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSnapshotTaskResult result = taskClient.getSnapshot(snapshotId);
        assertThat(snapshotId, equalTo(result.getSnapshotId()));
        assertThat(description, equalTo(result.getDescription()));
        assertThat(snapshotDate, equalTo(result.getSnapshotDate()));
        assertThat(host, equalTo(result.getSourceHost()));
        assertThat(spaceId, equalTo(result.getSourceSpaceId()));
        assertThat(storeId, equalTo(result.getSourceStoreId()));
        assertThat(snapshotStatus, equalTo(result.getStatus()));
    }

    @Test
    public void testCleanupSnapshot() throws Exception {
        String taskName = SnapshotConstants.CLEANUP_SNAPSHOT_TASK_NAME;

        CleanupSnapshotTaskResult preparedResult = new CleanupSnapshotTaskResult();
        preparedResult.setContentExpirationDays(contentExpirationDays);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        CleanupSnapshotTaskResult result = taskClient.cleanupSnapshot(spaceId);
        assertThat(contentExpirationDays,
                   equalTo(result.getContentExpirationDays()));
    }

    @Test
    public void testCompleteSnapshot() throws Exception {
        String taskName = SnapshotConstants.COMPLETE_SNAPSHOT_TASK_NAME;

        CompleteSnapshotTaskResult preparedResult = new CompleteSnapshotTaskResult();
        preparedResult.setResult(completionResult);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        CompleteSnapshotTaskResult result =
            taskClient.completeSnapshot(spaceId);
        assertThat(completionResult, equalTo(result.getResult()));
    }

    @Test
    public void testGetSnapshots() throws Exception {
        String taskName = SnapshotConstants.GET_SNAPSHOTS_TASK_NAME;

        List<SnapshotSummary> summaries = new ArrayList<>();
        summaries.add(
            new SnapshotSummary(snapshotId, snapshotStatus, description));
        GetSnapshotListTaskResult preparedResult =
            new GetSnapshotListTaskResult();
        preparedResult.setSnapshots(summaries);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSnapshotListTaskResult result = taskClient.getSnapshots();
        List<SnapshotSummary> resultSummaryList = result.getSnapshots();
        SnapshotSummary resultSummary = resultSummaryList.get(0);
        assertThat(snapshotId, equalTo(resultSummary.getSnapshotId()));
        assertThat(description, equalTo(resultSummary.getDescription()));
        assertThat(snapshotStatus, equalTo(resultSummary.getStatus()));
    }

    @Test
    public void testGetSnapshotContents() throws Exception {
        String taskName = SnapshotConstants.GET_SNAPSHOT_CONTENTS_TASK_NAME;

        Map<String, String> contentProps = new HashMap<>();
        contentProps.put(propName, propValue);
        SnapshotContentItem contentItem = new SnapshotContentItem();
        contentItem.setContentId(contentId);
        contentItem.setContentProperties(contentProps);
        List<SnapshotContentItem> contentItems = new ArrayList<>();
        contentItems.add(contentItem);

        GetSnapshotContentsTaskResult preparedResult =
            new GetSnapshotContentsTaskResult();
        preparedResult.setContentItems(contentItems);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetSnapshotContentsTaskResult result =
            taskClient.getSnapshotContents(snapshotId, 1, 1000, null);
        SnapshotContentItem item = result.getContentItems().get(0);
        assertThat(contentId, equalTo(item.getContentId()));
        assertThat(propValue, equalTo(item.getContentProperties().get(propName)));
    }

    @Test
    public void testRestoreSnapshot() throws Exception {
        String taskName = SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME;

        RestoreSnapshotTaskResult preparedResult = new RestoreSnapshotTaskResult();
        preparedResult.setSpaceId(spaceId);
        preparedResult.setRestoreId(restoreId);
        preparedResult.setStatus(restoreStatus);

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        RestoreSnapshotTaskResult result =
            taskClient.restoreSnapshot(snapshotId, userEmail);
        assertThat(spaceId, equalTo(result.getSpaceId()));
        assertThat(restoreId, equalTo(result.getRestoreId()));
        assertThat(restoreStatus, equalTo(result.getStatus()));
    }

    @Test
    public void testGetRestore() throws Exception {
        String taskName = SnapshotConstants.GET_RESTORE_TASK_NAME;

        GetRestoreTaskResult preparedResult = new GetRestoreTaskResult();
        preparedResult.setRestoreId(restoreId);
        preparedResult.setDestinationHost(host);
        preparedResult.setDestinationPort(port);
        preparedResult.setDestinationSpaceId(spaceId);
        preparedResult.setDestinationStoreId(storeId);
        preparedResult.setEndDate(snapshotDate);
        preparedResult.setSnapshotId(snapshotId);
        preparedResult.setStartDate(snapshotDate);
        preparedResult.setStatus(restoreStatus);
        preparedResult.setStatusText(restoreStatus.name());

        setupMock(taskName, preparedResult.serialize());
        replayMocks();

        GetRestoreTaskResult result = taskClient.getRestore(restoreId);
        assertThat(restoreId, equalTo(result.getRestoreId()));
        assertThat(host, equalTo(result.getDestinationHost()));
        assertThat(port, equalTo(result.getDestinationPort()));
        assertThat(spaceId, equalTo(result.getDestinationSpaceId()));
        assertThat(storeId, equalTo(result.getDestinationStoreId()));
        assertThat(snapshotDate, equalTo(result.getEndDate()));
        assertThat(snapshotId, equalTo(result.getSnapshotId()));
        assertThat(snapshotDate, equalTo(result.getStartDate()));
        assertThat(restoreStatus, equalTo(result.getStatus()));
        assertThat(restoreStatus.name(), equalTo(result.getStatusText()));
    }

}
