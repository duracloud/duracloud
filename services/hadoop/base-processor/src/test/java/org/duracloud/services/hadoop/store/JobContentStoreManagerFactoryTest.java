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
import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.storage.domain.HadoopTypes;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * @author Andrew Woods
 *         Date: Oct 12, 2010
 */
public class JobContentStoreManagerFactoryTest {

    private JobContentStoreManagerFactory factory0;
    private JobContentStoreManagerFactory factory1;

    private static final String username0 = "username-0";
    private static final String password0 = "password-0";
    private static final String username1 = "username-1";
    private static final String password1 = "password-1";

    @Test
    public void testGetContentStoreManager() throws Exception {
        factory0 = new JobContentStoreManagerFactory();
        factory1 = new JobContentStoreManagerFactory();

        JobConf jobConf0 = createJobConf(username0, password0);
        JobConf jobConf1 = createJobConf(username1, password1);

        ContentStoreManager mgr0 = factory0.getContentStoreManager(jobConf0);
        Assert.assertNotNull(mgr0);

        ContentStoreManager mgr1 = factory1.getContentStoreManager(jobConf1);
        Assert.assertNotNull(mgr1);
        Assert.assertTrue(mgr0 != mgr1);

        ContentStoreManager mgr = factory0.getContentStoreManager(jobConf1);
        Assert.assertNotNull(mgr);
        Assert.assertEquals(mgr1, mgr);
    }

    private JobConf createJobConf(String username, String password) {
        JobConf jobConf = new JobConf();
        jobConf.set(TASK_PARAMS.DC_HOST.getLongForm(), "testhost");
        jobConf.set(TASK_PARAMS.DC_PORT.getLongForm(), "8234");
        jobConf.set(TASK_PARAMS.DC_CONTEXT.getLongForm(), "testcontext");
        jobConf.set(TASK_PARAMS.DC_USERNAME.getLongForm(), username);
        jobConf.set(TASK_PARAMS.DC_PASSWORD.getLongForm(), password);
        return jobConf;
    }
}
