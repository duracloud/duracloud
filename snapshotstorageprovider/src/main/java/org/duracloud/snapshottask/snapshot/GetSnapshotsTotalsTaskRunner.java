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
import org.duracloud.snapshot.dto.task.GetSnapshotsTotalsTaskParameters;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get total count, size and files of snapshots which are accessible to this account.
 *
 * @author Nicholas Woodward
 * Date: 8/2/21
 */
public class GetSnapshotsTotalsTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(GetSnapshotsTotalsTaskRunner.class);

    private String dcHost;
    private String dcStoreId;
    private StorageProvider storageProvider;

    public GetSnapshotsTotalsTaskRunner(String dcHost,
                                        String dcStoreId,
                                        String bridgeAppHost,
                                        String bridgeAppPort,
                                        String bridgeAppUser,
                                        String bridgeAppPass,
                                        StorageProvider storageProvider) {
        super(bridgeAppHost, bridgeAppPort, bridgeAppUser, bridgeAppPass);
        this.dcHost = dcHost;
        this.dcStoreId = dcStoreId;
        this.storageProvider = storageProvider;
    }

    @Override
    public String getName() {
        return SnapshotConstants.GET_SNAPSHOTS_TOTALS_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        GetSnapshotsTotalsTaskParameters taskParams =
            GetSnapshotsTotalsTaskParameters.deserialize(taskParameters);

        // get bridge results
        String result = callBridge(createRestHelper(), buildBridgeURL(taskParams));

        return result;
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL(GetSnapshotsTotalsTaskParameters taskParams) {
        String status = taskParams.getStatus();

        return MessageFormat.format("{0}/snapshot/total?host={1}&storeId={2}&status={3}",
                                    buildBridgeBaseURL(),
                                    dcHost,
                                    dcStoreId,
                                    status);
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get total count, size, and files of snapshots. URL: {}", bridgeURL);

        try {
            RestHttpHelper.HttpResponse response = restHelper.get(bridgeURL);
            int statusCode = response.getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("Unexpected response code: " +
                                           statusCode);
            }
            return response.getResponseBody();
        } catch (Exception e) {
            throw new TaskException("Exception encountered attempting to " +
                                    "get total count, size, and files of snapshots. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
