/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.bulkimageconversion;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.BaseAmazonMapReduceJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.RUN_HADOOP_TASK_NAME;
import static org.duracloud.storage.domain.HadoopTypes.TASK_OUTPUTS;
import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Aug 18, 2010
 */
public class HadoopJobWorker extends BaseAmazonMapReduceJobWorker {

    public HadoopJobWorker(ContentStore contentStore,
                           String workSpaceId,
                           Map<String, String> taskParams,
                           String serviceWorkDir) {
        super(contentStore, workSpaceId, taskParams, serviceWorkDir);
    }

    @Override
    protected Map<String, String> getParamToResourceFileMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put(TASK_PARAMS.JAR_CONTENT_ID.name(),
                "image-conversion-processor.hjar");
        map.put(TASK_PARAMS.BOOTSTRAP_CONTENT_ID.name(),
                "install-image-magick.sh");

        return map;
    }

}
