/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.hadoop;

import org.duracloud.storage.domain.HadoopTypes;

import java.util.List;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.TASK_PARAMS;

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public class BulkImageConversionTaskHelper implements HadoopTaskHelper {

    /**
     * Add image conversion specific jar parameters
     */
    @Override
    public List<String> completeJarParams(Map<String, String> taskParams,
                                          List<String> jarParams) {
        String destFormat = taskParams.get(TASK_PARAMS.DEST_FORMAT.name());
        String namePrefix = taskParams.get(TASK_PARAMS.NAME_PREFIX.name());
        String nameSuffix = taskParams.get(TASK_PARAMS.NAME_SUFFIX.name());
        String colorSpace = taskParams.get(TASK_PARAMS.COLOR_SPACE.name());

        if(destFormat == null) {
            throw new RuntimeException("Destination format must be provided " +
                                       "to run image conversion hadoop job");
        }

        jarParams.add("-f");
        jarParams.add(destFormat);
        if(namePrefix != null && !namePrefix.equals("")) {
            jarParams.add("-p");
            jarParams.add(namePrefix);
        }
        if(nameSuffix != null && !nameSuffix.equals("")) {
            jarParams.add("-s");
            jarParams.add(nameSuffix);
        }
        if(colorSpace != null && !colorSpace.equals("")) {
            jarParams.add("-c");
            jarParams.add(colorSpace);
        }

        return jarParams;
    }
}
