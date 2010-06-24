/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image.osgi;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.image.ImageConversionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

/**
 * Performs a test of the Image Conversion Service from within an OSGi
 * container. This test requires that a DuraStore application be available
 * on localhost:8080.
 *
 * @author Bill Branan
 *         Date: Jan 27, 2010
 */
public class ImageConversionServiceTester {

    private ImageConversionService service;
    private String workDir;

    // This needs to be set (and should not be included in commit) in order
    // for this test to pass. This is available for testing in lieu of
    // including the unit-test-db project in the OSGi container. 
    private String rootPassword = "";

    private final static String BASE_DIR_PROP = "base.dir";

    public ImageConversionServiceTester(ImageConversionService service)
        throws IOException {
        this.service = service;

        File workDir = new File(service.getServiceWorkDir());
        workDir.mkdirs();
        this.workDir = workDir.getAbsolutePath();
    }

    public void testImageConversionService() throws Exception {
        String serviceWorkDir = service.getServiceWorkDir();
        assertNotNull(serviceWorkDir);
        assertTrue(new File(serviceWorkDir).exists());
        testStartStopCycle();
    }

    public void testStartStopCycle() throws Exception {
        // Set Up
        String random = getRandom();
        String sourceSpaceId = "conversion-test-source-" + random;
        String destSpaceId = "conversion-test-dest-" + random;
        service.setSourceSpaceId(sourceSpaceId);
        service.setDestSpaceId(destSpaceId);
        service.setUsername("root");
        service.setPassword(rootPassword);

        ContentStore contentStore = getContentStore();

        String testFileName = "testfile.tiff";
        File testFile = new File(getResourceDir(), testFileName);
        assertTrue(testFile.exists());
        contentStore.createSpace(sourceSpaceId, null);
        contentStore.addContent(sourceSpaceId,
                                testFileName,
                                new FileInputStream(testFile),
                                testFile.length(),
                                "image/tiff",
                                null,
                                null);

        try {
            // Start Conversion
            service.start();
            waitForConversion();

            // Stop Service
            service.stop();
            Thread.sleep(2000); // Wait to allow for shutdown

            // Make sure that converted file exists
            Map<String, String> meta =
                contentStore.getContentMetadata(destSpaceId, "testfile.jp2");
            assertNotNull(meta);
        } finally {
            // Clean up
            try {
                contentStore.deleteSpace(sourceSpaceId);
                contentStore.deleteSpace(destSpaceId);
            } catch(ContentStoreException e) {
                // Ignore
            }
        }
    }

    private ContentStore getContentStore() throws Exception {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(service.getDuraStoreHost(),
                                        service.getDuraStorePort(),
                                        service.getDuraStoreContext());
        storeManager.login(new Credential("root", rootPassword));
        return storeManager.getPrimaryContentStore();
    }

    private String getRandom() {
        return String.valueOf(new Random().nextInt(99999));
    }

    private void waitForConversion() throws Exception {
        for (int loops = 0; loops < 20 && !conversionComplete(); loops++) {
            Thread.sleep(5000); // Wait to allow for conversion
        }

        if(!conversionComplete()) {
            fail("Conversion did not complete, conversion status: " +
                getConversionStatus());
        }
    }

    private boolean conversionComplete() {
        String status = getConversionStatus();
        return status.contains("Complete");
    }

    private String getConversionStatus() {
        return service.getServiceProps().get("conversionStatus");
    }

    private String getResourceDir() {
        String baseDir = System.getProperty(BASE_DIR_PROP);
        assertNotNull(baseDir);

        return baseDir + File.separator + "src/test/resources/";
    }
    
}
