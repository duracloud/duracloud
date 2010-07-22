/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming.osgi;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.services.streaming.MediaStreamingService;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Performs a test of the Media Streaming Service from within an OSGi
 * container. Note that this test only ensures that the service can be
 * deployed into the container properly, it does not actually test the
 * setting up of a streaming distribution, as it can take up to 15 min
 * before streaming is actually available, and can take up to 24 hours
 * before streaming is disabled.
 *
 * @author Bill Branan
 *         Date: June 4, 2010
 */
public class MediaStreamingServiceTester {

    private MediaStreamingService service;
    private String workDir;

    public MediaStreamingServiceTester(MediaStreamingService service)
        throws IOException {
        this.service = service;

        File workDir = new File(service.getServiceWorkDir());
        workDir.mkdirs();
        this.workDir = workDir.getAbsolutePath();
    }

    public void testMediaStreamingService() throws Exception {
        String serviceWorkDir = service.getServiceWorkDir();
        assertNotNull(serviceWorkDir);
        assertTrue(new File(serviceWorkDir).exists());
        testStartStopCycle();
    }

    public void testStartStopCycle() throws Exception {
        Map<String, String> props = service.getServiceProps();
        assertNotNull(props);
    }

}
