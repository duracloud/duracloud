/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.storage;

import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.StorageProvider.AccessType;
import org.duracloud.storage.util.StorageProviderUtil;
import static org.duracloud.storage.util.StorageProviderUtil.compareChecksum;
import static org.duracloud.storage.util.StorageProviderUtil.contains;
import static org.duracloud.storage.util.StorageProviderUtil.count;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class is the functional test code across a StorageProvider.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTestCore
        implements StorageProvidersTestInterface {

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

        verifySpaceMetadata(provider, spaceId);
    }

    private boolean spaceExists(StorageProvider provider, String spaceId) {
        boolean exists;
        try {
            provider.getSpaceAccess(spaceId);
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

    private void verifySpaceMetadata(StorageProvider provider,
                                            String spaceId)
        throws StorageException {
        Map<String, String> metadata = provider.getSpaceMetadata(spaceId);
        assertNotNull(metadata);

        String created =
                metadata.get(StorageProvider.METADATA_SPACE_CREATED);
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

    public void testGetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        Map<String, String> spaceMd = null;
        spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);

        assertTrue(spaceMd.containsKey(StorageProvider.METADATA_SPACE_CREATED));
        assertTrue(spaceMd.containsKey(StorageProvider.METADATA_SPACE_COUNT));
        assertTrue(spaceMd.containsKey(StorageProvider.METADATA_SPACE_ACCESS));

        assertNotNull(spaceMd.get(StorageProvider.METADATA_SPACE_CREATED));
        assertNotNull(spaceMd.get(StorageProvider.METADATA_SPACE_COUNT));
        assertNotNull(spaceMd.get(StorageProvider.METADATA_SPACE_ACCESS));
    }

    public void testSetSpaceMetadata(StorageProvider provider, String spaceId0)
            throws StorageException {
        final String key0 = "key0";
        final String key1 = "key1";
        final String key2 = "key2";
        final String val0 = "val0";
        final String val1 = "val1";
        final String val2 = "val2";

        Map<String, String> spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);

        final int numProps = spaceMd.size();
        assertTrue(numProps > 0);

        // Add some props.
        Map<String, String> newMd = new HashMap<String, String>();
        newMd.put(key0, val0);
        newMd.put(key2, val2);
        provider.setSpaceMetadata(spaceId0, newMd);

        spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);
        assertEquals(numProps + 2, spaceMd.size());

        assertTrue(spaceMd.containsKey(key0));
        assertFalse(spaceMd.containsKey(key1));
        assertTrue(spaceMd.containsKey(key2));

        assertEquals(val0, spaceMd.get(key0));
        assertEquals(val2, spaceMd.get(key2));

        // Add some different props.
        Map<String, String> newerMd = new HashMap<String, String>();
        newerMd.put(key1, val1);
        newerMd.put(key2, val2);
        provider.setSpaceMetadata(spaceId0, newerMd);

        spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);
        assertEquals(numProps + 2, spaceMd.size());

        assertFalse(spaceMd.containsKey(key0));
        assertTrue(spaceMd.containsKey(key1));
        assertTrue(spaceMd.containsKey(key2));

        assertEquals(val1, spaceMd.get(key1));
        assertEquals(val2, spaceMd.get(key2));

    }

    public void testGetSpaceAccess(StorageProvider provider, String spaceId0)
            throws StorageException {
        AccessType access = null;

        // Test default access.
        access = provider.getSpaceAccess(spaceId0);
        Assert.assertEquals(AccessType.CLOSED, access);

        // ...also check Access in user metadata.
        Map<String, String> spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);
        String prop = spaceMd.get(StorageProvider.METADATA_SPACE_ACCESS);
        assertNotNull(prop);
        assertEquals(AccessType.CLOSED.toString(), prop);

        // Open access, and test again.
        provider.setSpaceAccess(spaceId0, AccessType.OPEN);
        access = provider.getSpaceAccess(spaceId0);
        Assert.assertEquals(AccessType.OPEN, access);

        // ...also check Access in user metadata.
        spaceMd = provider.getSpaceMetadata(spaceId0);
        assertNotNull(spaceMd);
        prop = spaceMd.get(StorageProvider.METADATA_SPACE_ACCESS);
        assertNotNull(prop);
        assertEquals(AccessType.OPEN.toString(), prop);

        // Set to Closed via metadata, test again
        spaceMd.put(StorageProvider.METADATA_SPACE_ACCESS,
                    AccessType.CLOSED.toString());
        provider.setSpaceMetadata(spaceId0, spaceMd);
        access = provider.getSpaceAccess(spaceId0);
        Assert.assertEquals(AccessType.CLOSED, access);        
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

    public void testSetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String spaceId1,
                                       String contentId0,
                                       String contentId1)
            throws StorageException {

        Map<String, String> contentMetadata = new HashMap<String, String>();
        final String key0 = "key0";
        final String key1 = "KEY1";
        final String val0 = "val0";
        final String val1 = "val1";
        contentMetadata.put(key0, val0);

        // Need to have a space and content.
        addContent(provider, spaceId1, contentId0, mimeText, "hello".getBytes());

        // Check initial state of metadata.
        Map<String, String> initialMeta =
                provider.getContentMetadata(spaceId1, contentId0);
        assertNotNull(initialMeta);
        int initialSize = initialMeta.size();
        assertTrue(initialSize > 0);
        assertEquals(mimeText,
                     initialMeta.get(StorageProvider.METADATA_CONTENT_MIMETYPE));

        // Set and check.
        provider.setContentMetadata(spaceId1, contentId0, contentMetadata);
        Map<String, String> metadata =
                provider.getContentMetadata(spaceId1, contentId0);
        assertNotNull(metadata);
        assertEquals(initialSize + 1, metadata.size());

        assertTrue(metadata.containsKey(key0));
        assertEquals(val0, metadata.get(key0));

        final String newVal = "newVal0";
        contentMetadata.put(key0, newVal);
        contentMetadata.put(key1, val1);

        provider.setContentMetadata(spaceId1, contentId0, contentMetadata);
        metadata = provider.getContentMetadata(spaceId1, contentId0);
        assertNotNull(metadata);
        assertEquals(initialSize + 2, metadata.size());
        assertTrue(metadata.containsKey(key0));
        assertEquals(newVal, metadata.get(key0));

        assertTrue(metadata.containsKey(key1.toLowerCase()));
        assertEquals(val1, metadata.get(key1.toLowerCase()));

        String mime = metadata.get(StorageProvider.METADATA_CONTENT_MIMETYPE);
        assertNotNull(mime);
        assertTrue(mime.startsWith(mimeText));

        // Set MIME and check.
        contentMetadata.put(StorageProvider.METADATA_CONTENT_MIMETYPE,
                            StorageProvider.DEFAULT_MIMETYPE);
        provider.setContentMetadata(spaceId1, contentId0, contentMetadata);
        metadata = provider.getContentMetadata(spaceId1, contentId0);
        assertNotNull(metadata);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE,
                     metadata.get(StorageProvider.METADATA_CONTENT_MIMETYPE));


        // Clear properties.
        provider.setContentMetadata(spaceId1,
                                    contentId0,
                                    new HashMap<String, String>());
        metadata = provider.getContentMetadata(spaceId1, contentId0);
        assertNotNull(metadata);
        assertEquals(initialSize, metadata.size());
        assertFalse(metadata.containsKey(key0));
        assertFalse(metadata.containsKey(key1));

        // Add content with null mimetype, should resolve to default
        addContent(provider, spaceId1, contentId1, null, "hello".getBytes());
        metadata = provider.getContentMetadata(spaceId1, contentId1);
        assertNotNull(metadata);
        assertEquals(StorageProvider.DEFAULT_MIMETYPE, 
                     metadata.get(StorageProvider.METADATA_CONTENT_MIMETYPE));
    }

    public void testGetContentMetadata(StorageProvider provider,
                                       String spaceId0,
                                       String contentId0)
            throws StorageException {
        final String mimeKey = StorageProvider.METADATA_CONTENT_MIMETYPE;
        final String sizeKey = StorageProvider.METADATA_CONTENT_SIZE;
        final String modifiedKey = StorageProvider.METADATA_CONTENT_MODIFIED;
        final String cksumKey = StorageProvider.METADATA_CONTENT_CHECKSUM;

        byte[] data = "hello-friends".getBytes();
        ChecksumUtil chksum = new ChecksumUtil(Algorithm.MD5);
        String digest = chksum.generateChecksum(new ByteArrayInputStream(data));

        addContent(provider, spaceId0, contentId0, mimeText, data);

        Map<String, String> metadata =
                provider.getContentMetadata(spaceId0, contentId0);
        assertNotNull(metadata);
        assertTrue(metadata.containsKey(mimeKey));
        assertTrue(metadata.containsKey(sizeKey));
        assertTrue(metadata.containsKey(modifiedKey));
        assertTrue(metadata.containsKey(cksumKey));

        assertEquals(mimeText, metadata.get(mimeKey));
        assertEquals(data.length, Integer.parseInt(metadata.get(sizeKey)));
        assertNotNull(metadata.get(modifiedKey));
        assertEquals(digest, metadata.get(cksumKey));

        // Set and check again.
        provider.setContentMetadata(spaceId0,
                                    contentId0,
                                    new HashMap<String, String>());
        metadata = provider.getContentMetadata(spaceId0, contentId0);
        assertNotNull(metadata);
        assertTrue(metadata.containsKey(mimeKey));
        assertTrue(metadata.containsKey(sizeKey));
        assertTrue(metadata.containsKey(modifiedKey));
        assertTrue(metadata.containsKey(cksumKey));

        // Mimetype value is unchanged.
        String mime = metadata.get(mimeKey);
        assertNotNull(mime);
        assertTrue(mime.startsWith(mimeText));
        assertEquals(data.length, Integer.parseInt(metadata.get(sizeKey)));
        assertNotNull(metadata.get(modifiedKey));
        assertEquals(digest, metadata.get(cksumKey));

        // Set and check again.
        metadata = new HashMap<String, String>();
        metadata.put(mimeKey, mimeXml);
        provider.setContentMetadata(spaceId0,
                                    contentId0,
                                    metadata);
        metadata = provider.getContentMetadata(spaceId0, contentId0);
        assertNotNull(metadata);
       
        // Mimetype value is updated
        assertEquals(mimeXml, metadata.get(mimeKey));
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
    //        // FIXME: The test below would work if metadata were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertNotNull(spaces);
    //        //        assertTrue(spaces.contains(spaceId1));
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        EsuApi visitor = createVisitor();
    //        ObjectMetadata allMd = visitor.getAllMetadata(rootId);
    //        assertNotNull(allMd);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.CLOSED);
    //        access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.CLOSED, access);
    //
    //        // FIXME: The test below would work if metadata were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertEquals(null, spaces);
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        try {
    //            visitor.getAllMetadata(rootId);
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
    //        // FIXME: The test below would work if metadata were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertNotNull(spaces);
    //        //        assertTrue(spaces.contains(spaceId1));
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        EsuApi visitor = createVisitor();
    //        ObjectMetadata allMd = visitor.getAllMetadata(objId);
    //        assertNotNull(allMd);
    //
    //        provider.setSpaceAccess(spaceId1, AccessType.CLOSED);
    //        access = provider.getSpaceAccess(spaceId1);
    //        assertEquals(AccessType.CLOSED, access);
    //
    //        // FIXME: The test below would work if metadata were available across users.
    //        //        List<String> spaces = createVisitorProvider().getSpaces();
    //        //        assertEquals(null, spaces);
    //
    //        // FIXME: The 'createVisitor' test should be removed when the above works.
    //        try {
    //            visitor.getAllMetadata(objId);
    //            fail("Exception expected.");
    //        } catch (Exception e) {
    //        }
    //    }


}
