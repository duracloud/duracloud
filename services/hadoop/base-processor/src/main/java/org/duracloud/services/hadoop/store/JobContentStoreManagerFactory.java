/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.apache.hadoop.mapred.JobConf;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;

import java.util.HashMap;
import java.util.Map;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * @author Andrew Woods
 *         Date: Oct 12, 2010
 */
public class JobContentStoreManagerFactory {

    private static Map<Credential, ContentStoreManager> storeManagers = new HashMap<Credential, ContentStoreManager>();

    public ContentStoreManager getContentStoreManager(JobConf jobConf) {
        String dcHost = getParam(jobConf, TASK_PARAMS.DC_HOST.getLongForm());
        String dcPort = getParam(jobConf, TASK_PARAMS.DC_PORT.getLongForm());
        String dcCtxt = getParam(jobConf, TASK_PARAMS.DC_CONTEXT.getLongForm());
        String dcUser = getParam(jobConf, TASK_PARAMS.DC_USERNAME.getLongForm());
        String dcPass = getParam(jobConf, TASK_PARAMS.DC_PASSWORD.getLongForm());

        Credential credential = new Credential(dcUser, dcPass);
        ContentStoreManager mgr = storeManagers.get(credential);
        if (null == mgr) {
            mgr = new ContentStoreManagerImpl(dcHost, dcPort, dcCtxt);
            mgr.login(credential);

            storeManagers.put(credential, mgr);
        }

        return mgr;
    }

    private String getParam(JobConf jobConf, String param) {
        String value = jobConf.get(param);
        if (null == value) {
            throw new RuntimeException("Required param not found: " + param);
        }

        return value;
    }

}
