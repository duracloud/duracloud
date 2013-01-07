/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.error.NotFoundException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StorageProviderBaseTest {

    private StorageProviderBaseImpl providerBase;
    private StorageProviderBase providerMock;

    private final static String spaceId = "space-id";
    private static Map<String, String> spaceProps;
    private static Map<String, AclType> spaceACLs;
    private static Map<String, String> userProps;

    private final static String user0 = "user-0";
    private final static String user1 = "user-1";
    private final static String group0 = "group-0";

    private final static String propName0 = "unknown-name";

    private final static String mimePrefix =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    private final static String aclPrefix =
        StorageProvider.PROPERTIES_SPACE_ACL;

    @Before
    public void setUp() throws Exception {
        providerMock = EasyMock.createMock("StorageProviderBase",
                                           StorageProviderBase.class);
        providerBase = new StorageProviderBaseImpl(providerMock);

        spaceACLs = new HashMap<String, AclType>();
        spaceACLs.put(aclPrefix + user0, AclType.READ);
        spaceACLs.put(aclPrefix + group0, AclType.READ);
        spaceACLs.put(aclPrefix + user1, AclType.WRITE);

        userProps = new HashMap<String, String>();
        userProps.put(propName0, "unknown-value");
        userProps.put(mimePrefix, "text/plain");

        spaceProps = new HashMap<String, String>();
        spaceProps.putAll(userProps);
        for (String key : spaceACLs.keySet()) {
            spaceProps.put(key, spaceACLs.get(key).name());
        }
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

        Assert.assertEquals(userProps.size(), props.size());
        Set<String> propKeys = props.keySet();
        for (String key : userProps.keySet()) {
            Assert.assertTrue(propKeys.contains(key));
            Assert.assertEquals(userProps.get(key), props.get(key));
        }
    }

    private void createGetSpacePropertiesMocks() {
        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            spaceProps);

        EasyMock.makeThreadSafe(providerMock, true);
        replayMocks();
    }

    @Test
    public void testGetSpaceACLs() {
        createGetSpaceACLsMocks();

        Map<String, AclType> acls = providerBase.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);

        Assert.assertEquals(spaceACLs.size(), acls.size());
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

        Map<String, AclType> newProps = new HashMap<String, AclType>();
        String name0 = aclPrefix + "name0";
        String name1 = "name1";
        String name2 = aclPrefix + "name2";
        String name3 = mimePrefix + "name3";

        AclType value0 = AclType.READ;
        AclType value1 = AclType.READ;
        AclType value2 = AclType.READ;
        AclType value3 = AclType.READ;

        newProps.put(name0, value0);
        newProps.put(name1, value1);
        newProps.put(name2, value2);
        newProps.put(name3, value3);

        Map<String, String> expectedProps = new HashMap<String, String>();
        expectedProps.put(name0, value0.name());
        expectedProps.put(name2, value2.name());
        expectedProps.put(propName0, spaceProps.get(propName0));
        expectedProps.put(mimePrefix, spaceProps.get(mimePrefix));

        providerMock.doSetSpaceProperties(spaceId, expectedProps);
        EasyMock.expectLastCall();

        replayMocks();

        // This test passes if the mock objects verify.
        providerBase.setSpaceACLs(spaceId, newProps);
    }

    @Test
    public void testDeleteSpace() {
        providerMock.throwIfSpaceNotExist(spaceId);
        EasyMock.expectLastCall();

        EasyMock.expect(providerMock.getAllSpaceProperties(spaceId)).andReturn(
            new HashMap<String, String>()).once();

        providerMock.doSetSpaceProperties(EasyMock.<String>anyObject(),
                                          EasyMock.<Map<String, String>>anyObject());
        EasyMock.expectLastCall();

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
        EasyMock.expectLastCall();

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
        EasyMock.expectLastCall();

        EasyMock.expect(providerMock.getSpaceContents(EasyMock.eq(spaceId),
                                                      EasyMock.<String>isNull()))
                .andReturn(new ArrayList<String>().iterator())
                .once();

        providerMock.removeSpace(spaceId);
        EasyMock.expectLastCall();

        replayMocks();

        StorageProviderBase.SpaceDeleteWorker worker =
            providerBase.getSpaceDeleteWorker(spaceId);
        worker.run();
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
        EasyMock.expectLastCall();

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
        EasyMock.expectLastCall();

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

        public InputStream getContent(String spaceId, String contentId) {
            return mock.getContent(spaceId, contentId);
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
