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

/**
 * @author: Bill Branan
 * Date: Aug 23, 2010
 */
public interface HadoopTaskHelper {

    /**
     * Complete the listing of jar parameters by adding any task-specific
     * parameters to the list.
     *
     * @param taskParams map of parameters passed to the task, which may be
     *                   helpful in determining jar parameters to included
     * @param jarParams the generic set of jar parameters that can be added
     *                  to for a specific type of hadoop job
     * @return
     */
    public List<String> completeJarParams(Map<String, String> taskParams,
                                          List<String> jarParams);
    
}
