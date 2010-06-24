/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import org.duracloud.common.model.Credential;
import org.duracloud.s3storage.S3ProviderTestBase;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3TaskProviderTest extends S3ProviderTestBase {

    private S3TaskProvider taskProvider;

    @Before
    public void setUp() throws Exception {
        Credential s3Credential = getCredential();
        taskProvider = new S3TaskProvider(s3Credential.getUsername(), 
                                          s3Credential.getPassword());
    }

    @Test
    public void testPerformTask() throws Exception {
        List<String> supportedTasks = taskProvider.getSupportedTasks();
        assertNotNull(supportedTasks);

        String noopResult = taskProvider.performTask("noop", "");
        assertNotNull(noopResult);

        try {
            taskProvider.performTask("unsupported-task", "parameters");
            fail("Exception expected performing unknown task");
        } catch(UnsupportedTaskException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testGetTaskStatus() throws Exception {
        try {
            taskProvider.performTask("unsupported-task", "parameters");
            fail("Exception expected performing unknown task");
        } catch(UnsupportedTaskException expected) {
            assertNotNull(expected);
        }        
    }
}
