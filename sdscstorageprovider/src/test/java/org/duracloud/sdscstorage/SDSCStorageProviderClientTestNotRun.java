/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sdscstorage;

import junit.framework.Assert;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.storage.error.StorageException;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.openstack.swift.SwiftApiMetadata;
import org.jclouds.openstack.swift.SwiftAsyncClient;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.ContainerMetadata;
import org.jclouds.openstack.swift.domain.ObjectInfo;
import org.jclouds.openstack.swift.options.ListContainerOptions;
import org.jclouds.rest.RestContext;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Tests the ability of the SDSC provider to make it through
 *
 * @author Bill Branan
 *         Date: 7/29/13
 */
public class SDSCStorageProviderClientTestNotRun {

    private final Logger log = LoggerFactory
        .getLogger(SDSCStorageProviderClientTestNotRun.class);

    private static String authUrl =
        "https://duracloud.auth.cloud.sdsc.edu/auth/v1.0";

    private int plannedAttempts = 3;
    private String username;
    private String password;

    @Before
    public void setup() throws Exception {
        TestConfigUtil configUtil = new TestConfigUtil();
        SimpleCredential credential = configUtil.getCredential(
            StorageProviderCredential.ProviderType.SDSC);
        Assert.assertNotNull(credential);

        this.username = credential.getUsername();
        this.password = credential.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
     }

    private SDSCStorageProvider getFreshStorageProvider() {
        return new SDSCStorageProvider(username, password);
    }

    private SwiftClient getFreshSwiftClient() {
        String trimmedAuthUrl = // JClouds expects authURL with no version
            authUrl.substring(0, authUrl.lastIndexOf("/"));
        return ContextBuilder.newBuilder(new SwiftApiMetadata())
                             .endpoint(trimmedAuthUrl)
                             .credentials(username, password)
                             .buildApi(SwiftClient.class);
    }

    @Test
    public void testSDSCProvider() throws Exception {
        log.info("STARTING SDSC PROVIDER ITERATION TEST");

        int iterationCompletedCount = 0;
        for(int i=0; i<plannedAttempts; i++) {
            try {
                iterateThroughContent(getFreshStorageProvider());
                iterationCompletedCount++;
            } catch(Exception e) {
                log.error(
                    "Iteration failure on attempt: " + i + ". Error message: " +
                        e.getMessage());
            }
        }
        log.info("SDSC Provider iteration attempts: " + plannedAttempts +
                 "; Iterations completed: " + iterationCompletedCount);

        log.info("SDSC PROVIDER ITERATION TEST COMPLETE");
    }

    private void iterateThroughContent(SDSCStorageProvider sdscProvider) {
        long spaceCount = 0;
        long contentCount = 0;
        long errorCount = 0;

        Iterator<String> spaces = sdscProvider.getSpaces();

        while(spaces.hasNext()) {
            spaceCount++;
            String spaceId = spaces.next();
            Iterator<String> items =
                sdscProvider.getSpaceContents(spaceId, null);
            while(items.hasNext()) {
                contentCount++;
                String contentId = items.next();
                try {
                    Map<String, String> contentMeta =
                        sdscProvider.getContentProperties(spaceId, contentId);
                } catch(StorageException e) {
                    errorCount++;
                    log.error("Exception getting content: " + e.getMessage() +
                              ". Root Cause: " + getRootCause(e));
                    if(e.getMessage().contains("401 Unauthorized")) {
                        log.warn("Throwing due to 401");
                        printCount(spaceCount, contentCount, errorCount);
                        throw new RuntimeException("401 Unauthorized error");
                    }
                }
            }
            printCount(spaceCount, contentCount, errorCount);
        }
    }

    @Test
    public void testSwiftClient() throws Exception {
        log.info("STARTING JCLOUDS SWIFT CLIENT ITERATION TEST");

        int iterationCompletedCount = 0;
        for(int i=0; i<plannedAttempts; i++) {
            try {
                iterateThroughContentSwiftClient(getFreshSwiftClient());
                iterationCompletedCount++;
            } catch(Exception e) {
                log.error("Iteration failure on attempt: " + i +
                          ". Error message: " + e.getMessage());
            }
        }
        log.info("Swift Client iteration attempts: " + plannedAttempts +
                 "; Iterations completed: " + iterationCompletedCount);

        log.info("JCLOUDS SWIFT CLIENT ITERATION TEST COMPLETE");
    }

    private void iterateThroughContentSwiftClient(SwiftClient swiftClient) {
        long spaceCount = 0;
        long contentCount = 0;
        long errorCount = 0;

        // Get the list of containers
        Set<ContainerMetadata> containers =
            swiftClient.listContainers(new ListContainerOptions());

        // Loop through each contaner
        for(ContainerMetadata container : containers) {
            spaceCount++;
            String containerId = container.getName();

            String marker = null;
            PageSet<ObjectInfo> contentItems =
                swiftClient.listObjects(containerId, new ListContainerOptions());

            // Loop through each content item
            while(null != contentItems && contentItems.size() > 0) {
                for(ObjectInfo contentItem : contentItems) {
                    String contentId = contentItem.getName();
                    contentCount++;
                    try {
                        // Get the metadata for the content item
                        swiftClient.getObjectInfo(containerId, contentId);
                    } catch(Exception e) {
                        errorCount++;
                                log.error("Exception getting content: " +
                                          e.getMessage() +
                                          ". Root Cause: " + getRootCause(e));
                    }
                    marker = contentId;
                }
                // Get the next set of items
                ListContainerOptions listOptions = new ListContainerOptions();
                listOptions.afterMarker(marker);
                contentItems = swiftClient.listObjects(containerId, listOptions);
            }
            printCount(spaceCount, contentCount, errorCount);
        }
    }

    private void printCount(long spaceCount, long contentCount, long errorCount) {
        log.info("Space count: " + spaceCount);
        log.info("Content count:" + contentCount);
        log.info("Error count:" + errorCount);
    }

    private String getRootCause(Throwable t) {
        while(t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }

}
