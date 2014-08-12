/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.GetSnapshotContentsTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Gets the list of content items that are contained in the snapshot. This is
 * the same as the list of content that existed in the original space at the
 * moment the snapshot was initiated.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotContentsTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(GetSnapshotsTaskRunner.class);

    public GetSnapshotContentsTaskRunner(String bridgeAppHost,
                                         String bridgeAppPort,
                                         String bridgeAppUser,
                                         String bridgeAppPass) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
    }

    @Override
    public String getName() {
        return SnapshotConstants.GET_SNAPSHOT_CONTENTS_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetSnapshotContentsTaskParameters taskParams =
            GetSnapshotContentsTaskParameters.deserialize(taskParameters);
        return callBridge(createRestHelper(), buildBridgeURL(taskParams));
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(GetSnapshotContentsTaskParameters taskParams) {
        int pageNumber = taskParams.getPageNumber();
        if(pageNumber < SnapshotConstants.DEFAULT_CONTENT_PAGE_NUMBER) {
            pageNumber = SnapshotConstants.DEFAULT_CONTENT_PAGE_NUMBER;
        }
        int pageSize = taskParams.getPageSize();
        if(pageSize < SnapshotConstants.MIN_CONTENT_PAGE_SIZE ||
           pageSize > SnapshotConstants.MAX_CONTENT_PAGE_SIZE) {
            pageSize = SnapshotConstants.MAX_CONTENT_PAGE_SIZE;
        }
        String snapshotId = taskParams.getSnapshotId();
        String prefix = taskParams.getPrefix();
        String prefixParam = "";
        if(null != prefix) {
            prefixParam = "&prefix=" + prefix;
        }

        return MessageFormat.format(
            "{0}/snapshot/{1}/content?page={2}&pageSize={3}{4}",
            buildBridgeBaseURL(),
            snapshotId,
            String.valueOf(pageNumber),
            String.valueOf(pageSize),
            prefixParam);
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get snapshot contents. URL: {}", bridgeURL);

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
                                    "get snapshot contents. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
