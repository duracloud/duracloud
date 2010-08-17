/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.imageconversion;

import org.apache.hadoop.mapred.JobConf;

/**
 * @author: Bill Branan
 * Date: Aug 16, 2010
 */
public class HadoopTestUtil {

    public static JobConf createJobConf() {
        // An unnecessary stack track is printed when creating a JobConf
        // See org.apache.hadoop.conf.Configuration line 211
        System.out.println("--- BEGIN EXPECTED STACK TRACE ---");
        JobConf conf = new JobConf();
        System.out.println("--- END EXPECTED STACK TRACE ---");
        return conf;
    }

}
