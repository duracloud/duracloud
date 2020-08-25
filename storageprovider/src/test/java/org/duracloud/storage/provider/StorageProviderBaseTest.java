/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.NotFoundException;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StorageProviderBaseTest {

    private StorageProviderBaseImpl providerBase;
    private StorageProviderBase providerMock;

    private final static String spaceId = "space-id";
    private static Map<String, String> spaceProps;
    private static Map<String, String> spaceACLs;
    private static Map<String, String> systemProps;

    private final static String userRead0 = "user-read-0";
    private final static String userRead1 = "user-read-1";
    private final static String userWrite0 = "user-write-0";
    private final static String groupRead0 = "group-read-0";
    private final static String groupWrite0 = "group-write-0";
    private final static String groupWrite1 = "group-write-1";

    private final static String sysPropName = "space-created";
    private final static String sysPropVal = new Date().toString();

    private final static String mimePrefix =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    private final static String aclPrefix =
        StorageProvider.PROPERTIES_SPACE_ACL;
    private final static String aclGroupPrefix =
        StorageProvider.PROPERTIES_SPACE_ACL_GROUP;

    @Before
    public void setUp() throws Exception {
        providerMock = EasyMock.createMock("StorageProviderBase",
                                           StorageProviderBase.class);
        providerBase = new StorageProviderBaseImpl(providerMock);

        String delim = StorageProviderBase.ACL_DELIM;
        spaceACLs = new HashMap<>();
        spaceACLs.put(StorageProviderBase.ACL_USER_READ,
                      userRead0 + delim + userRead1 + delim);
        spaceACLs.put(StorageProviderBase.ACL_USER_WRITE, userWrite0 + delim);
        spaceACLs.put(StorageProviderBase.ACL_GROUP_READ, groupRead0 + delim);
        spaceACLs.put(StorageProviderBase.ACL_GROUP_WRITE,
                      groupWrite0 + delim + groupWrite1 + delim);

        systemProps = new HashMap<>();
        systemProps.put(sysPropName, sysPropVal);

        spaceProps = new HashMap<>();
        spaceProps.putAll(systemProps);
        spaceProps.putAll(spaceACLs);
    }

    @After
    public void tearDown() {
        EasyMock.verify(providerMock);
    }

    private void replayMocks() {
        EasyMock.replay(providerMock);
    }

    @Test
    public void testGetSpaceProperties() {
        createGetSpacePropertiesMocks();

        Map<String, String> props = providerBase.getSpaceProperties(spaceId);
        Assert.assertNotNull(props);

        Assert.assertEquals(systemProps.size(), props.size());
        Assert.assertTrue(systemProps.containsKey(sysPropName));
        Assert.assertEquals(sysPropVal, systemProps.get(sysPropName));
    }

    private void createGetSpacePropertiesMocks() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            spaceProps);

        EasyMock.makeThreadSafe(providerMock, true);
        replayMocks();
    }

    @Test
    public void testSetNewSpacePropertiesMaintainACLs() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId))
                .andReturn(spaceProps);

        Capture<Map<String, String>> propsSetCapture =
            Capture.newInstance(CaptureType.FIRST);
        providerMock.doSetSpaceProperties(EasyMock.eq(spaceId),
                                          EasyMock.capture(propsSetCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        Map<String, String> newProps = new HashMap<>();
        String newPropName = "newPropName";
        String newPropValue = "newPropValue";
        newProps.put(newPropName, newPropValue);

        // Run the test
        providerBase.setNewSpaceProperties(spaceId, newProps);

        Map<String, String> propsSet = propsSetCapture.getValue();
        Assert.assertNotNull(propsSet);

        // Verify results, new prop should be in place
        Assert.assertEquals(newPropValue, propsSet.get(newPropName));

        // Verify results, old props should be gone
        Assert.assertFalse(propsSet.containsKey(sysPropName));

        // Verify results, all ACLs should be unchanged
        String userRead = propsSet.get(StorageProviderBase.ACL_USER_READ);
        Assert.assertTrue(userRead.contains(userRead0));
        Assert.assertTrue(userRead.contains(userRead1));

        String userWrite = propsSet.get(StorageProviderBase.ACL_USER_WRITE);
        Assert.assertTrue(userWrite.contains(userWrite0));

        String groupRead = propsSet.get(StorageProviderBase.ACL_GROUP_READ);
        Assert.assertTrue(groupRead.contains(groupRead0));

        String groupWrite = propsSet.get(StorageProviderBase.ACL_GROUP_WRITE);
        Assert.assertTrue(groupWrite.contains(groupWrite0));
        Assert.assertTrue(groupWrite.contains(groupWrite1));
    }

    @Test
    public void testSetNewSpacePropertiesNewACLs() {
        Capture<Map<String, String>> propsSetCapture =
            Capture.newInstance(CaptureType.FIRST);
        providerMock.doSetSpaceProperties(EasyMock.eq(spaceId),
                                          EasyMock.capture(propsSetCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        Map<String, String> newProps = new HashMap<>();
        String newPropName = "newPropName";
        String newPropValue = "newPropValue";
        newProps.put(newPropName, newPropValue);

        Map<String, AclType> newACLs = new HashMap<>();
        String newAclUser = "newAclUser";
        newACLs.put(StorageProvider.PROPERTIES_SPACE_ACL + newAclUser, AclType.READ);

        // Run the test
        providerBase.setNewSpaceProperties(spaceId, newProps, newACLs);

        Map<String, String> propsSet = propsSetCapture.getValue();
        Assert.assertNotNull(propsSet);

        // Verify results, new prop should be in place
        Assert.assertEquals(newPropValue, propsSet.get(newPropName));

        // Verify results, old props should be gone
        Assert.assertFalse(propsSet.containsKey(sysPropName));

        // Verify results, new ACLs should be in place
        String userRead = propsSet.get(StorageProviderBase.ACL_USER_READ);
        Assert.assertTrue(userRead.contains(newAclUser));

        // Verify results, old ACLs should be gone
        Assert.assertFalse(userRead.contains(userRead0));
    }

    @Test
    public void testGetSpaceACLs() {
        createGetSpaceACLsMocks();

        Map<String, AclType> acls = providerBase.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);

        Assert.assertEquals(6, acls.size()); // users + groups = 6
        Assert.assertEquals(AclType.READ, acls.get(aclPrefix + userRead0));
        Assert.assertEquals(AclType.READ, acls.get(aclPrefix + userRead1));
        Assert.assertEquals(AclType.WRITE, acls.get(aclPrefix + userWrite0));
        Assert.assertEquals(AclType.READ,
                            acls.get(aclGroupPrefix + groupRead0));
        Assert.assertEquals(AclType.WRITE,
                            acls.get(aclGroupPrefix + groupWrite0));
        Assert.assertEquals(AclType.WRITE,
                            acls.get(aclGroupPrefix + groupWrite0));
    }

    private void createGetSpaceACLsMocks() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            spaceProps);

        EasyMock.makeThreadSafe(providerMock, true);
        replayMocks();
    }

    @Test
    public void testSetSpaceACLs() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            spaceProps);

        Map<String, AclType> newProps = new HashMap<>();
        newProps.put(aclPrefix + userRead0, AclType.READ);
        newProps.put(aclPrefix + userRead1, AclType.READ);
        newProps.put(aclPrefix + userWrite0, AclType.WRITE);
        newProps.put(aclGroupPrefix + groupRead0, AclType.READ);
        newProps.put(aclGroupPrefix + groupWrite0, AclType.WRITE);
        newProps.put(aclGroupPrefix + groupWrite1, AclType.WRITE);

        Capture<Map<String, String>> propsSetCapture =
            Capture.newInstance(CaptureType.FIRST);
        providerMock.doSetSpaceProperties(EasyMock.eq(spaceId),
                                          EasyMock.capture(propsSetCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        // Run the test
        providerBase.setSpaceACLs(spaceId, newProps);

        // Verify results
        Map<String, String> propsSet = propsSetCapture.getValue();
        Assert.assertNotNull(propsSet);

        String userRead = propsSet.get(StorageProviderBase.ACL_USER_READ);
        Assert.assertNotNull(userRead);
        Assert.assertTrue(userRead.contains(userRead0));
        Assert.assertTrue(userRead.contains(userRead1));

        String userWrite = propsSet.get(StorageProviderBase.ACL_USER_WRITE);
        Assert.assertNotNull(userWrite);
        Assert.assertTrue(userWrite.contains(userWrite0));

        String groupRead = propsSet.get(StorageProviderBase.ACL_GROUP_READ);
        Assert.assertNotNull(groupRead);
        Assert.assertTrue(groupRead.contains(groupRead0));

        String groupWrite = propsSet.get(StorageProviderBase.ACL_GROUP_WRITE);
        Assert.assertNotNull(groupWrite);
        Assert.assertTrue(groupWrite.contains(groupWrite0));
        Assert.assertTrue(groupWrite.contains(groupWrite1));
    }

    @Test
    public void testPackUnpackACLs() {
        Map<String, AclType> unpackedACLs = providerBase.unpackACLs(spaceACLs);
        Map<String, String> repackedACLs = providerBase.packACLs(unpackedACLs);

        // Verify equality (considering that ordering may differ)
        for (String aclGrouping : spaceACLs.keySet()) {
            String[] original = spaceACLs.get(aclGrouping)
                                         .split(StorageProviderBase.ACL_DELIM);
            Arrays.sort(original);
            String[] repacked = repackedACLs.get(aclGrouping)
                                            .split(StorageProviderBase.ACL_DELIM);
            Arrays.sort(repacked);
            Assert.assertArrayEquals(original, repacked);
        }

        replayMocks();
    }

    @Test
    public void testSetSpaceACLsEmpty() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            spaceProps);

        Map<String, AclType> newProps = new HashMap<>();

        Capture<Map<String, String>> propsSetCapture =
            Capture.newInstance(CaptureType.FIRST);
        providerMock.doSetSpaceProperties(EasyMock.eq(spaceId),
                                          EasyMock.capture(propsSetCapture));
        EasyMock.expectLastCall().once();

        replayMocks();

        // Run the test
        providerBase.setSpaceACLs(spaceId, newProps);

        // Verify results
        Map<String, String> propsSet = propsSetCapture.getValue();
        Assert.assertNotNull(propsSet);
        Assert.assertEquals(systemProps, propsSet);
    }

    @Test
    public void testDeleteSpace() {
        providerMock.throwIfSpaceNotExist(spaceId);
        EasyMock.expectLastCall().once();

        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            new HashMap<String, String>()).once();

        providerMock.doSetSpaceProperties(EasyMock.<String>anyObject(),
                                          EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall().once();

        replayMocks();

        providerBase.deleteSpace(spaceId);
    }

    @Test
    public void testEmptyDeleteWorker() {
        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(new ArrayList<String>().iterator())
                .once();

        providerMock.removeSpace(spaceId);
        EasyMock.expectLastCall().once();

        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testOnceDeleteWorker() {
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(contents.iterator())
                .once();

        providerMock.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall().once();

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(new ArrayList<String>().iterator())
                .once();

        providerMock.removeSpace(spaceId);
        EasyMock.expectLastCall().once();

        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testOnceDeleteWorkerWithWrappedStorageProvider() {
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);
        StorageProvider wrappedProvider = EasyMock.createMock(StorageProvider.class);
        providerBase.setWrappedStorageProvider(wrappedProvider);
        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(contents.iterator())
                .once();

        wrappedProvider.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall().once();

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(new ArrayList<String>().iterator())
                .once();

        providerMock.removeSpace(spaceId);
        EasyMock.expectLastCall().once();
        EasyMock.replay(wrappedProvider);
        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();

        EasyMock.verify(wrappedProvider);
    }

    @Test
    public void testOnceMultipleDeleteWorker() {
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);
        contents.add(contentId);

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(contents.iterator())
                .once();

        providerMock.deleteContent(spaceId, contentId);
        EasyMock.expectLastCall().times(2);

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(new ArrayList<String>().iterator())
                .once();

        providerMock.removeSpace(spaceId);
        EasyMock.expectLastCall().once();

        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    @Test
    public void testRetriesDeleteWorker() {
        String contentId = "content-id";
        List<String> contents = new ArrayList<String>();
        contents.add(contentId);

        EasyMock.expect(providerMock.getSpaceContents(spaceId, null)).andReturn(
            contents.iterator());

        // 5 tries, 5 failures
        for (int i = 0; i < 5; ++i) {
            EasyMock.expect(providerMock.getSpaceContents(spaceId, null))
                    .andReturn(contents.iterator());

            providerMock.deleteContent(spaceId, contentId);
            EasyMock.expectLastCall().andThrow(new NotFoundException(""));
        }

        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            new HashMap<String, String>());

        providerMock.doSetSpaceProperties(EasyMock.<String>anyObject(),
                                          EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall().once();

        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();
    }

    /**
     * This is an implementation of the abstract StorageProviderBase class,
     * which is the class actually under test.
     */
    public class StorageProviderBaseImpl extends StorageProviderBase {

        private StorageProviderBase mock;

        public StorageProviderBaseImpl(StorageProviderBase mock) {
            super();
            this.mock = mock;
        }

        public StorageProviderType getStorageProviderType() {
            return mock.getStorageProviderType();
        }

        protected boolean spaceExists(String spaceId) {
            return mock.spaceExists(spaceId);
        }

        protected void removeSpace(String spaceId) {
            mock.removeSpace(spaceId);
        }

        public Iterator<String> getSpaces() {
            return mock.getSpaces();
        }

        protected void throwIfSpaceNotExist(String spaceId) {
            mock.throwIfSpaceNotExist(spaceId);
        }

        public RetrievedContent getContent(String spaceId, String contentId) {
            return mock.getContent(spaceId, contentId);
        }

        public RetrievedContent getContent(String spaceId, String contentId, String range) {
            return mock.getContent(spaceId, contentId, range);
        }

        public Iterator<String> getSpaceContents(String spaceId,
                                                 String prefix) {
            return mock.getSpaceContents(spaceId, prefix);
        }

        public List<String> getSpaceContentsChunked(String spaceId,
                                                    String prefix,
                                                    long maxResults,
                                                    String marker) {
            return mock.getSpaceContentsChunked(spaceId,
                                                prefix,
                                                maxResults,
                                                marker);
        }

        public void createSpace(String spaceId) {
            mock.createSpace(spaceId);
        }

        protected Map<String, String> getAllSpaceProperties(String spaceId) {
            return mock.getAllSpaceProperties(spaceId);
        }

        @Override
        protected void doSetSpaceProperties(String spaceId,
                                            Map<String, String> spaceProps) {
            mock.doSetSpaceProperties(spaceId, spaceProps);
        }

        public String addContent(String spaceId,
                                 String contentId,
                                 String contentMimeType,
                                 Map<String, String> userProperties,
                                 long contentSize,
                                 String contentChecksum,
                                 InputStream content) {
            return mock.addContent(spaceId,
                                   contentId,
                                   contentMimeType,
                                   userProperties,
                                   contentSize,
                                   contentChecksum,
                                   content);
        }

        @Override
        public String copyContent(String sourceSpaceId,
                                  String sourceContentId,
                                  String destSpaceId,
                                  String destContentId) {
            return mock.copyContent(sourceSpaceId,
                                    sourceContentId,
                                    destSpaceId,
                                    destContentId);
        }

        public void deleteContent(String spaceId, String contentId) {
            mock.deleteContent(spaceId, contentId);
        }

        public void setContentProperties(String spaceId,
                                         String contentId,
                                         Map<String, String> contentProperties) {
            mock.setContentProperties(spaceId, contentId, contentProperties);
        }

        public Map<String, String> getContentProperties(String spaceId,
                                                        String contentId) {
            return mock.getContentProperties(spaceId, contentId);
        }
    }
}
