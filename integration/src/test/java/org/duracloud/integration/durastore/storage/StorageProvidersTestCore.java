/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage;

import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderUtil;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.contains;
import static org.duracloud.storage.util.StorageProviderUtil.count;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class is the functional test code across a StorageProvider.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTestCore
        implements StorageProvidersTestInterface {
    protected final static Logger log = LoggerFactory.getLogger(
        StorageProvidersTestCore.class);

    private final String mimeText = "text/plain";

    private final String mimeXml = "text/xml";

    private final String spaceIdInvalid = "duracloud-invalid-space-id";

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void testGetSpaces(StorageProvider provider,
                              String spaceId0,
                              String spaceId1) throws StorageException {
        Iterator<String> spaces = provider.getSpaces();
        assertNotNull(spaces);

        List<String> spaceList = StorageProviderUtil.getList(spaces);
        assertTrue(spaceList.size() >= 2);

        assertTrue(spaceList.contains(spaceId0));
        assertTrue(spaceList.contains(spaceId1));
    }

    public void testGetSpaceContents(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1) throws StorageException {
        Iterator<String> spaceContents = null;
        try {
            spaceContents = provider.getSpaceContents(spaceIdInvalid, null);
            Assert.fail("Exception should be thrown if space does not exist.");
        } catch (Exception e) {
        }

        // First content to add
        byte[] content0 = "hello world.".getBytes();
        addContent(provider, spaceId0, contentId0, mimeText, content0);

        // Second content to add
        byte[] content1 = "<a>hello</a>".getBytes();
        addContent(provider, spaceId0, contentId1, mimeXml, content1);

        spaceContents = provider.getSpaceContents(spaceId0, null);
        assertNotNull(spaceContents);
        assertEquals(2, count(spaceContents));

        spaceContents = provider.getSpaceContents(spaceId0, null);
        assertNotNull(spaceContents);
        assertTrue(contains(spaceContents, contentId0));

        spaceContents = provider.getSpaceContents(spaceId0, null);
        assertNotNull(spaceContents);
        assertTrue(contains(spaceContents, contentId1));
    }

    public void testGetSpaceContentsChunked(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1) throws StorageException {
        // TODO: EMC does not support chunked content listing yet
    }

    private void addContent(StorageProvider provider,
                            String spaceKey,
                            String contentKey,
                            String mime,
                            byte[] data) throws StorageException {
        addContent(provider, spaceKey, contentKey, mime, data, false);
    }

    private void addContent(StorageProvider provider,
                            String spaceKey,
                            String contentKey,
                            String mime,
                            byte[] data,
                            boolean checksumInAdvance) throws StorageException {
        ByteArrayInputStream contentStream = new ByteArrayInputStream(data);

        String advChecksum = null;
        if(checksumInAdvance) {
            ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            advChecksum = util.generateChecksum(contentStream);
            contentStream.reset();
        }

        String checksum =
                provider.addContent(spaceKey,
                                    contentKey,
                                    mime,
                                    null,
                                    data.length,
                                    advChecksum,
                                    contentStream);
        if(checksumInAdvance) {
           assertEquals(advChecksum, checksum);
        }

        compareChecksum(provider, spaceKey, contentKey, checksum);
    }

    public void testCreateSpace(StorageProvider provider, String spaceId)
            throws StorageException {
        provider.createSpace(spaceId);

        int maxLoops = 20;
        for (int loops = 0;
             !spaceExists(provider, spaceId) && loops < maxLoops;
             loops++) {
            sleep(2000);
        }

        verifySpaceProperties(provider, spaceId);
    }

    private boolean spaceExists(StorageProvider provider, String spaceId) {
        boolean exists;
        try {
            provider.getSpaceACLs(spaceId);
            exists = true;
        } catch (NotFoundException e) {
            exists = false;
        }

        if(exists) {
            try {
                Iterator<String> spaces = provider.getSpaces();
                assertNotNull(spaces);
                exists = contains(spaces, spaceId);
            } catch (StorageException e) {
                exists = false;
            }
        }

        return exists;
    }

    private void verifySpaceProperties(StorageProvider provider,
                                            String spaceId)
        throws StorageException {
        Map<String, String> properties = provider.getSpaceProperties(spaceId);
        assertNotNull(properties);

        String created =
                properties.get(StorageProvider.PROPERTIES_SPACE_CREATED);
        assertNotNull(created);
    }

    public void testDeleteSpace(StorageProvider provider,
                                String spaceId)
        throws StorageException {
        try {
            provider.deleteSpace(spaceId);

            Iterator<String> spaces = provider.getSpaces();
            assertNotNull(spaces);
            assertFalse(contains(spaces, spaceId));
        } catch (Exception e) {
        }
    }

    public void testGetSpaceProperties(StorageProvider provider, String spaceId0)
            throws StorageException {
        Map<String, String> spaceMd = null;
        spaceMd = provider.getSpaceProperties(spaceId0);
        assertNotNull(spaceMd);

        assertTrue(spaceMd.containsKey(StorageProvider.PROPERTIES_SPACE_CREATED));
        assertTrue(spaceMd.containsKey(StorageProvider.PROPERTIES_SPACE_COUNT));

        assertNotNull(spaceMd.get(StorageProvider.PROPERTIES_SPACE_CREATED));
        assertNotNull(spaceMd.get(StorageProvider.PROPERTIES_SPACE_COUNT));
    }

    public void testAddAndGetContent(StorageProvider provider,
                                     String spaceId0,
                                     String contentId0,
                                     String contentId1,
                                     String contentId2) throws Exception {
        byte[] content0 = "hello,world.".getBytes();

        // First content to add
        addContent(provider, spaceId0, contentId1, mimeText, content0, true);

        // Second content to add
        byte[] content1 = "<a>hello</a>".getBytes();
        addContent(provider, spaceId0, contentId2, mimeXml, content1, true);

        // Verify content on retrieval.
        InputStream is0 = provider.getContent(spaceId0, contentId1);
        assertNotNull(is0);
        assertEquals(new String(content0), getData(is0));

        InputStream is1 = provider.getContent(spaceId0, contentId2);
        assertNotNull(is1);
        assertEquals(new String(content1), getData(is1));

    }

    public void testAddAndGetContentOverwrite(StorageProvider provider,
                                              String spaceId0,
                                              String contentId0,
                                              String contentId1)
            throws Exception {
        byte[] content0 = "hello,world.".getBytes();

        // First content to add
        addContent(provider, spaceId0, contentId0, mimeText, content0);

        // Second content to add
        byte[] content1 = "<a>hello</a>".getBytes();
        addContent(provider, spaceId0, contentId1, mimeXml, content1);

        // Verify content on retrieval.
        InputStream is0 = provider.getContent(spaceId0, contentId0);
        assertNotNull(is0);
        assertEquals(new String(content0), getData(is0));

        // Overwrite existing content
        addContent(provider, spaceId0, contentId0, mimeXml, content1);
        InputStream is1 = provider.getContent(spaceId0, contentId0);
        assertNotNull(is1);
        assertEquals(new String(content1), getData(is1));
    }

    public void testAddContentLarge(StorageProvider provider,
                                    String spaceId0,
                                    String contentId0,
                                    String contentId1) throws Exception {
        // TODO: maybe turn this test on?
        System.err.println("==================");
        System.err.println("This test is not run because it "
                + "uploads 16MB of data to the StorageProvider: "
                + "StorageProvidersTestCore.testAddContentLarge()");
        System.err.println("==================");

        if (false) {
            // Create large data object (16MB)
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000000; ++i) {
                sb.append("xxxxxxxxxxxxxxxx");
            }
            byte[] content0 = sb.toString().getBytes();

            // First content to add
            addContent(provider, spaceId0, contentId1, mimeText, content0);

            // Verify content on retrieval.
            InputStream is0 = provider.getContent(spaceId0, contentId1);
            assertNotNull(is0);
            assertEquals(new String(content0), getData(is0));
        }
    }

    private String getData(InputStream is) throws IOException {
        List<Byte> bytes = new ArrayList<Byte>();
        byte b = (byte) is.read();
        while (b != -1) {
            bytes.add(b);
            b = (byte) is.read();
        }

        byte[] data = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); ++i) {
            data[i] = bytes.get(i);
        }
        return new String(data);
    }

    public void testDeleteContent(StorageProvider provider,
                                  String spaceId0,
                                  String contentId0,
                                  String contentId1) throws StorageException {
        // Test with non-existant space
        try {
            provider.deleteContent(spaceId0, contentId0);
            fail("Exception expected.");
        } catch (Exception e) {
        }

        Iterator<String> spaceContents =
            provider.getSpaceContents(spaceId0, null);
        verifyContentListing(spaceContents);

        // Test with valid space, non-existant content
        try {
            provider.deleteContent(spaceId0, contentId0);
            fail("Exception expected.");
        } catch (Exception e) {
        }

        // Add some content.
        byte[] data = "sample-text".getBytes();
        addContent(provider, spaceId0, contentId0, mimeText, data);

        spaceContents = provider.getSpaceContents(spaceId0, null);
        verifyContentListing(spaceContents, contentId0);

        // Add more content.
        addContent(provider, spaceId0, contentId1, mimeText, data);

        spaceContents = provider.getSpaceContents(spaceId0, null);
        verifyContentListing(spaceContents, contentId0, contentId1);

        // Delete content
        provider.deleteContent(spaceId0, contentId1);
        spaceContents = provider.getSpaceContents(spaceId0, null);
        verifyContentListing(spaceContents, contentId0);

        provider.deleteContent(spaceId0, contentId0);
        spaceContents = provider.getSpaceContents(spaceId0, null);
        verifyContentListing(spaceContents);
    }

    private void verifyContentListing(Iterator<String> listing,
                                      String... entries) {
        assertNotNull(listing);
        int count = 0;
        while (listing.hasNext()) {
            ++count;
            String listingItem = listing.next();
            boolean entryMatch = false;
            for (String entry : entries) {
                if (listingItem.equals(entry)) {
                    entryMatch = true;
                }
            }
            assertTrue(entryMatch);
        }
        assertEquals(entries.length, count);
    }

    public void testSetContentProperties(StorageProvider provider,
                                       String spaceId0,
                                       String spaceId1,
                                       String contentId0,
                                       String contentId1)
            throws StorageException {

        Map<String, String> contentProperties = new HashMap<String, String>();
        final String key0 = "key0";
        final String key1 = "KEY1";
        final String val0 = "val0";
        final String val1 = "val1";
        contentProperties.put(key0, val0);

        // Need to have a space and content.
        addContent(provider, spaceId1, contentId0, mimeText, "hello".getBytes());

        // Check initial state of properties.
        Map<String, String> initialMeta =
                provider.getContentProperties(spaceId1, contentId0);
        assertNotNull(initialMeta);
        int initialSize = initialMeta.size();
        assertTrue(initialSize > 0);
        assertEquals(mimeText,
                     initialMeta.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE));

        // Set and check.
        provider.setContentProperties(spaceId1, contentId0, contentProperties);
        Map<String, String> properties =
                provider.getContentProperties(spaceId1, contentId0);
        assertNotNull(properties);
        assertEquals(initialSize + 1, properties.size());

        assertTrue(properties.containsKey(key0));
        assertEquals(val0, properties.get(key0));

        final String newVal = "newVal0";
        contentProperties.put(key0, newVal);
        contentProperties.put(key1, val1);

        provider.setContentProperties(spaceId1, contentId0, contentProperties);
        properties = provider.getContentProperties(spaceId1, contentId0);
        assertNotNull(properties);
        assertEquals(initialSize + 2, properties.size());
        assertTrue(properties.containsKey(key0));
        assertEquals(newVal, properties.get(key0));

        assertTrue(properties.containsKey(key1.toLowerCase()));
        assertEquals(val1, properties.get(key1.toLowerCase()));

        String mime = properties.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE);
        assertNotNull(mime);
        assertTrue(mime.startsWith(mimeText));

        // Set MIME and check.
        contentProperties.put(StorageProvider.PROPERTIES_CONTENT_MIMETYPE,
                            StorageProvider.DEFAULT_MIMETYPE);
        provider.setContentProperties(spaceId1, contentId0, contentProperties);
        properties = provider.getContentProperties(spaceId1, contentId0);
        assertNotNull(properties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     properties.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE));


        // Clear properties.
        provider.setContentProperties(spaceId1,
                                    contentId0,
                                    new HashMap<String, String>());
        properties = provider.getContentProperties(spaceId1, contentId0);
        assertNotNull(properties);
        assertEquals(initialSize, properties.size());
        assertFalse(properties.containsKey(key0));
        assertFalse(properties.containsKey(key1));

        // Add content with null mimetype, should resolve to default
        addContent(provider, spaceId1, contentId1, null, "hello".getBytes());
        properties = provider.getContentProperties(spaceId1, contentId1);
        assertNotNull(properties);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     properties.get(StorageProvider.PROPERTIES_CONTENT_MIMETYPE));
    }

    public void testGetContentProperties(StorageProvider provider,
                                       String spaceId0,
                                       String contentId0)
            throws StorageException {
        final String mimeKey = StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
        final String sizeKey = StorageProvider.PROPERTIES_CONTENT_SIZE;
        final String modifiedKey = StorageProvider.PROPERTIES_CONTENT_MODIFIED;
        final String cksumKey = StorageProvider.PROPERTIES_CONTENT_CHECKSUM;

        byte[] data = "hello-friends".getBytes();
        ChecksumUtil chksum = new ChecksumUtil(Algorithm.MD5);
        String digest = chksum.generateChecksum(new ByteArrayInputStream(data));

        addContent(provider, spaceId0, contentId0, mimeText, data);

        Map<String, String> properties =
                provider.getContentProperties(spaceId0, contentId0);
        assertNotNull(properties);
        assertTrue(properties.containsKey(mimeKey));
        assertTrue(properties.containsKey(sizeKey));
        assertTrue(properties.containsKey(modifiedKey));
        assertTrue(properties.containsKey(cksumKey));

        assertEquals(mimeText, properties.get(mimeKey));
        assertEquals(data.length, Integer.parseInt(properties.get(sizeKey)));
        assertNotNull(properties.get(modifiedKey));
        assertEquals(digest, properties.get(cksumKey));

        // Set and check again.
        provider.setContentProperties(spaceId0,
                                    contentId0,
                                    new HashMap<String, String>());
        properties = provider.getContentProperties(spaceId0, contentId0);
        assertNotNull(properties);
        assertTrue(properties.containsKey(mimeKey));
        assertTrue(properties.containsKey(sizeKey));
        assertTrue(properties.containsKey(modifiedKey));
        assertTrue(properties.containsKey(cksumKey));

        // Mimetype value is unchanged.
        String mime = properties.get(mimeKey);
        assertNotNull(mime);
        assertTrue(mime.startsWith(mimeText));
        assertEquals(data.length, Integer.parseInt(properties.get(sizeKey)));
        assertNotNull(properties.get(modifiedKey));
        assertEquals(digest, properties.get(cksumKey));

        // Set and check again.
        properties = new HashMap<String, String>();
        properties.put(mimeKey, mimeXml);
        provider.setContentProperties(spaceId0,
                                    contentId0,
                                    properties);
        properties = provider.getContentProperties(spaceId0, contentId0);
        assertNotNull(properties);

        // Mimetype value is updated
        assertEquals(mimeXml, properties.get(mimeKey));
    }

    public void close() {
    }

    //
    //    public void testSpaceAccess() throws Exception {
    //        Identifier rootId = provider.getRootId(spaceId1);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.OPEN);
    //
    //        AccessType access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.OPEN, access);
    //
    //        // FIXME: The test below would work if properties were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertNotNull(spaces);
    //        //        assertTrue(spaces.contains(spaceId1));
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        EsuApi visitor = createVisitor();
    //        ObjectProperties allMd = visitor.getAllProperties(rootId);
    //        assertNotNull(allMd);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.CLOSED);
    //        access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.CLOSED, access);
    //
    //        // FIXME: The test below would work if properties were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertEquals(null, spaces);
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        try {
    //            visitor.getAllProperties(rootId);
    //            fail("Exception expected.");
    //        } catch (Exception e) {
    //        }
    //    }
    //
    //
    //    public void testContentAccess() throws Exception {
    //        addContent(spaceId1, contentId1, mimeText, "testing-content".getBytes());
    //        Identifier objId = provider.getContentObjId(spaceId1, contentId1);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.OPEN);
    //
    //        AccessType access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.OPEN, access);
    //
    //        // FIXME: The test below would work if properties were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertNotNull(spaces);
    //        //        assertTrue(spaces.contains(spaceId1));
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        EsuApi visitor = createVisitor();
    //        ObjectProperties allMd = visitor.getAllProperties(objId);
    //        assertNotNull(allMd);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.CLOSED);
    //        access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.CLOSED, access);
    //
    //        // FIXME: The test below would work if properties were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertEquals(null, spaces);
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        try {
    //            visitor.getAllProperties(objId);
    //            fail("Exception expected.");
    //        } catch (Exception e) {
    //        }
    //    }

    /**
     * This nested class spins on the abstract doCall() until the expected
     * result is returned or the maximum number of tries has been reached.
     *
     * @param <T> object type returned from abstract doCall()
     */
    private static abstract class StoreCaller<T> {

        public void call(T expected) {
            boolean callComplete = false;
            int maxTries = 5;
            int tries = 0;

            while (!callComplete && tries < maxTries) {
                try {
                    callComplete = expected.equals(doCall());
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        // do nothing
                    }
                }
                tries++;
            }
            Assert.assertTrue(
                expected + " not found after " + tries + " tries.",
                callComplete);
        }

        protected abstract T doCall() throws Exception;
    }

}
