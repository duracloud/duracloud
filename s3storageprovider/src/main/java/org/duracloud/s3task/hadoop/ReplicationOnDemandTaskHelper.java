/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;


import java.util.List;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;
import static org.duracloud.storage.domain.HadoopTypes.HJAR_PARAMS;

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
        String dcHost = taskParams.get(TASK_PARAMS.DC_HOST.name());
        String dcPort = taskParams.get(TASK_PARAMS.DC_PORT.name());
        String dcContext = taskParams.get(TASK_PARAMS.DC_CONTEXT.name());
        String dcUsername = taskParams.get(TASK_PARAMS.DC_USERNAME.name());
        String dcPassword = taskParams.get(TASK_PARAMS.DC_PASSWORD.name());

        if(sourceSpaceId == null || repStoreId == null ||
           repSpaceId == null || dcHost == null ||
           dcUsername == null || dcPassword == null) {
            throw new RuntimeException("All required parameters not provided");
        }

        jarParams.add(HJAR_PARAMS.SOURCE_SPACE_ID.getParam());
        jarParams.add(sourceSpaceId);
        jarParams.add(HJAR_PARAMS.REP_STORE_ID.getParam());
        jarParams.add(repStoreId);
        jarParams.add(HJAR_PARAMS.REP_SPACE_ID.getParam());
        jarParams.add(repSpaceId);
        jarParams.add(HJAR_PARAMS.DC_HOST.getParam());
        jarParams.add(dcHost);
        if(dcPort != null && !dcPort.equals("")) {
            jarParams.add(HJAR_PARAMS.DC_PORT.getParam());
            jarParams.add(dcPort);
        }
        if(dcContext != null && !dcContext.equals("")) {
            jarParams.add(HJAR_PARAMS.DC_CONTEXT.getParam());
            jarParams.add(dcContext);
        }
        jarParams.add(HJAR_PARAMS.DC_USERNAME.getParam());
        jarParams.add(dcUsername);
        jarParams.add(HJAR_PARAMS.DC_PASSWORD.getParam());
        jarParams.add(dcPassword);

        return jarParams;
    }
}
