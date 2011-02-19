/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity;

import org.duracloud.client.ContentStore;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;
import org.duracloud.storage.domain.HadoopTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Andrew Woods
 * Date: Sept 21, 2010
 */
public class AmazonFixityMetadataJobWorker extends BaseAmazonMapReduceJobWorker {

    public AmazonFixityMetadataJobWorker(ContentStore contentStore,
                                         String workSpaceId,
                                         Map<String, String> taskParams,
                                         String serviceWorkDir) {
        super(contentStore, workSpaceId, taskParams, serviceWorkDir);
    }

    @Override
    protected Map<String, String> getParamToResourceFileMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(HadoopTypes.TASK_PARAMS.JAR_CONTENT_ID.name(),
                "fixity-metadata-processor.hjar");
        return map;
    }

}
