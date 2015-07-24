/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.text.MessageFormat;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.GetSnapshotHistoryTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gets the list of history items that are contained in the snapshot.
 *
 * @author Gad Krumholz
 *         Date: 7/23/14
 */
public class GetSnapshotHistoryTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(GetSnapshotHistoryTaskRunner.class);

    public GetSnapshotHistoryTaskRunner(String bridgeAppHost,
                                         String bridgeAppPort,
                                         String bridgeAppUser,
                                         String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
    }

    @Override
    public String getName() {
        return SnapshotConstants.GET_SNAPSHOT_HISTORY_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetSnapshotHistoryTaskParameters taskParams =
                GetSnapshotHistoryTaskParameters.deserialize(taskParameters);
        return callBridge(createRestHelper(), buildBridgeURL(taskParams));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(GetSnapshotHistoryTaskParameters taskParams) {
        int pageNumber = taskParams.getPageNumber();
        if(pageNumber < SnapshotConstants.DEFAULT_HISTORY_PAGE_NUMBER) {
            pageNumber = SnapshotConstants.DEFAULT_HISTORY_PAGE_NUMBER;
        }
        int pageSize = taskParams.getPageSize();
        if(pageSize < SnapshotConstants.MIN_HISTORY_PAGE_SIZE ||
           pageSize > SnapshotConstants.MAX_HISTORY_PAGE_SIZE) {
            pageSize = SnapshotConstants.MAX_HISTORY_PAGE_SIZE;
        }
        String snapshotId = taskParams.getSnapshotId();
        
        return MessageFormat.format(
            "{0}/snapshot/{1}/history?page={2}&pageSize={3}",
            buildBridgeBaseURL(),
            snapshotId,
            String.valueOf(pageNumber),
            String.valueOf(pageSize));
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get snapshot history. URL: {}", bridgeURL);

        try {
            RestHttpHelper.HttpResponse response = restHelper.get(bridgeURL);
            int statusCode = response.getStatusCode();
            if(statusCode != 200) {
                throw new RuntimeException("Unexpected response code: " +
                                           statusCode);
            }
            return response.getResponseBody();
        } catch(Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "get snapshot history. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
