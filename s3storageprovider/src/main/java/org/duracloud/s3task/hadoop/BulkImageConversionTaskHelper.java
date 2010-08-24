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
        String destFormat = taskParams.get("destFormat");
        String namePrefix = taskParams.get("namePrefix");
        String nameSuffix = taskParams.get("nameSuffix");
        String colorSpace = taskParams.get("colorSpace");

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
