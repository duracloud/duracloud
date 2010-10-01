/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replicationod;

import org.duracloud.client.ContentStore;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;

import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Sep 30, 2010
 */
public class ReplicationOnDemandJobWorker extends BaseAmazonMapReduceJobWorker {

    public ReplicationOnDemandJobWorker(ContentStore contentStore,
                                        String workSpaceId,
                                        Map<String, String> taskParams,
                                        String serviceWorkDir) {
        super(contentStore, workSpaceId, taskParams, serviceWorkDir);
    }
    
    @Override
    protected Map<String, String> getParamToResourceFileMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TASK_PARAMS.JAR_CONTENT_ID.name(),
                "replication-processor.hjar");
        return map;
    }
}
