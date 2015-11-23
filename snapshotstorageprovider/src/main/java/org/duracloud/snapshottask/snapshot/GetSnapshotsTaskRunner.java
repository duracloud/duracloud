/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.SnapshotSummary;
import org.duracloud.snapshot.dto.bridge.GetSnapshotListBridgeResult;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Get a listing of snapshots which are accessible to this account.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotsTaskRunner extends AbstractSnapshotTaskRunner {

    private Logger log = LoggerFactory.getLogger(GetSnapshotsTaskRunner.class);

    private String dcHost;
    private String dcStoreId;
    private StorageProvider storageProvider;

    public GetSnapshotsTaskRunner(String dcHost,
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
        return SnapshotConstants.GET_SNAPSHOTS_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        //get bridge results
        String result =  callBridge(createRestHelper(), buildBridgeURL());
        
        //if the caller has only user privs
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!auth.getAuthorities().contains("ROLE_ADMIN")){
            //deserialize results
            GetSnapshotListBridgeResult list = GetSnapshotListBridgeResult.deserialize(result);
            List<SnapshotSummary> filteredSnapshots = new LinkedList<>();

            //create space set
            Iterator<String> it = this.storageProvider.getSpaces();
            Set<String> spaceSet = new HashSet<>();
            while(it.hasNext()){
                spaceSet.add(it.next());
            }

            //filter out all snapshots of spaces not in the set.
            for(SnapshotSummary snapshot : list.getSnapshots()){
                if(spaceSet.contains(snapshot.getSourceSpaceId())){
                    filteredSnapshots.add(snapshot);
                }
            }

            list.setSnapshots(filteredSnapshots);
            result = list.serialize();
        }
        
        return result;
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeURL() {
        return MessageFormat.format("{0}/snapshot?host={1}&storeId={2}",
                                    buildBridgeBaseURL(),
                                    dcHost,
                                    dcStoreId);
    }

    /*
     * Calls the bridge application to get snapshot listing
     */
    protected String callBridge(RestHttpHelper restHelper, String bridgeURL) {
        log.info("Making bridge call to get snapshot list. URL: {}", bridgeURL);

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
                                    "get list of snapshots. " +
                                    "Error reported: " + e.getMessage(), e);
        }
    }

}
