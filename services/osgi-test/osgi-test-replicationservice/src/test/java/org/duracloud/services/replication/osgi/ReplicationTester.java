/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replication.osgi;

import junit.framework.Assert;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.replication.ReplicationService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

/**
 * @author Andrew Woods
 *         Date: Mar 30, 2010
 */
public class ReplicationTester {
    private ReplicationService replicationService;
    private final String storeIdS3 = "1";
    private final String storeIdRackspace = "5";

    private ContentStore contentStore;
    private String spaceId;
    private String contentId;

    public ReplicationTester(ReplicationService replicationService) {
        Assert.assertNotNull(replicationService);
        this.replicationService = replicationService;
    }

    public void testReplication() {

        try {
            doTest();
        } catch (Exception e) {
        } finally {
            try {
                replicationService.stop();
            } catch (Exception e) {
            }
            replicationService = null;
            deleteSpace(spaceId);
            contentStore = null;
            spaceId = null;
            contentId = null;

        }
    }

    private void deleteSpace(String spaceId) {
        try {
            contentStore.deleteSpace(spaceId);
        } catch (Exception e) {
        }
    }

    private void doTest() throws Exception {
        // FIXME: this test hangs the pax container when activated.
        // see: https://jira.duraspace.org/browse/DURACLOUD-120
//        here(0);
//        replicationService.start();
//        here(1);
//
//        createContentStore();
//        String spaceId = getSpaceId();
//        String contentId = getContentId();
//
//        here(2);
//        createSpace(spaceId);
//
//
//        here(3);
//        String md5 = addContent(spaceId, contentId);
//
//        here(4);
//        replicationService.stop();
//        here(5);
    }

    private void here(int i) {
        System.out.println("--------------------here: " + i);
    }

    private ContentStore createContentStore() throws ContentStoreException {
        ContentStoreManager contentStoreManager = new ContentStoreManagerImpl(
            "localhost",
            "8080");
        Assert.assertNotNull(contentStoreManager);
        contentStoreManager.login(new SystemUserCredential());

        contentStore = contentStoreManager.getContentStore(storeIdS3);
        Assert.assertNotNull(contentStore);

        return contentStore;
    }

    private String getSpaceId() {
        String base = "test-repl-space-id-";
        spaceId = base + new Random().nextInt();
        return spaceId;
    }

    private String getContentId() {
        String base = "test-repl-content-id-";
        contentId = base + new Random().nextInt();
        return contentId;

    }

    private void createSpace(String spaceId)
        throws ContentStoreException {
        Map<String, String> metadata = null;
        contentStore.createSpace(spaceId, metadata);

        boolean created = false;
        int maxTries = 10;
        int tries = 0;
        while (!created && tries < maxTries) {
            try {
                contentStore.getSpaceAccess(spaceId);
                created = true;
            } catch (Exception e) {
                tries++;
                sleep(500);
            }
        }
    }

    private String addContent(String spaceId, String contentId)
        throws ContentStoreException {
        String text = "hello";
        InputStream content = new ByteArrayInputStream(text.getBytes());

        long contentSize = text.length();
        String mime = "text/plain";
        Map<String, String> metadata = null;

        return contentStore.addContent(spaceId,
                                       contentId,
                                       content,
                                       contentSize,
                                       mime,
                                       null,
                                       metadata);
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
