/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop.param;

import java.util.List;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public class ReplicationOnDemandTaskHelper implements HadoopTaskHelper {

    /**
     * Add image conversion specific jar parameters
     */
    @Override
    public List<String> completeJarParams(Map<String, String> taskParams,
                                          List<String> jarParams) {
        String sourceSpaceId = taskParams.get(TASK_PARAMS.SOURCE_SPACE_ID.name());
        String repStoreId = taskParams.get(TASK_PARAMS.REP_STORE_ID.name());
        String repSpaceId = taskParams.get(TASK_PARAMS.REP_SPACE_ID.name());

        if (sourceSpaceId == null || repStoreId == null || repSpaceId == null) {
            throw new RuntimeException("All required parameters not provided");
        }

        jarParams.add(TASK_PARAMS.SOURCE_SPACE_ID.getCliForm());
        jarParams.add(sourceSpaceId);
        jarParams.add(TASK_PARAMS.REP_STORE_ID.getCliForm());
        jarParams.add(repStoreId);
        jarParams.add(TASK_PARAMS.REP_SPACE_ID.getCliForm());
        jarParams.add(repSpaceId);
        
        return jarParams;
    }
}
