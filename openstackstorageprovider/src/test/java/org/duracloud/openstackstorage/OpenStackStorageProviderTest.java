/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.openstackstorage;

import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.internal.PageSetImpl;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.ContainerMetadata;
import org.jclouds.openstack.swift.domain.ObjectInfo;
import org.jclouds.openstack.swift.domain.internal.ObjectInfoImpl;
import org.jclouds.openstack.swift.options.ListContainerOptions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 1/16/13
 */
public class OpenStackStorageProviderTest {

    private OpenStackStorageProvider provider;
    private SwiftClient swiftClient;
    private String spaceId = "space-id";

    @Before
    public void setUp() throws Exception {
        swiftClient = EasyMock.createMock("SwiftClient", SwiftClient.class);
        provider = new OpenStackTestProvider(swiftClient);

    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(swiftClient);
    }

    private void replayMocks() {
        EasyMock.replay(swiftClient);
    }

    @Test
    public void testGetSpaceContentsChunked() throws Exception {
        String prefix = "this&is+the prefix";
        Long maxResults = 2L;
        String marker = "this&is+the marker";

        EasyMock.expect(swiftClient.containerExists(spaceId)).andReturn(true);

        List<ObjectInfo> objects = new ArrayList<ObjectInfo>();
        objects.add(ObjectInfoImpl.builder().container(spaceId)
                .name("test-content").build());
        PageSet<ObjectInfo> filesObjectInfo = new PageSetImpl<ObjectInfo>(objects, null);
        ListContainerOptions containerOptions =
                ListContainerOptions.Builder.maxResults(maxResults.intValue());
        containerOptions = containerOptions.afterMarker(provider.sanitizeForURI(marker));
        containerOptions = containerOptions.withPrefix(provider.sanitizeForURI(prefix));

        EasyMock.expect(swiftClient.listObjects(spaceId,
                containerOptions))
                .andReturn(filesObjectInfo);

        replayMocks();

        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   prefix,
                                                                   maxResults,
                                                                   marker);
        Assert.assertNotNull(contentIds);
    }

    @Test
    public void testSetSpaceProperties() {
        EasyMock.expect(swiftClient.containerExists(spaceId)).andReturn(true);

        Map<String, String> spaceProps = new HashMap<>();
        spaceProps.put("one", "two");
        spaceProps.put(StorageProvider.PROPERTIES_SPACE_CREATED, "2020-20-20");

        Capture<Map<String, String>> pushedPropsCap = new Capture<>();
        EasyMock.expect(
            swiftClient.setContainerMetadata(EasyMock.eq(spaceId),
                                             EasyMock.capture(pushedPropsCap)))
                .andReturn(true);

        replayMocks();

        provider.doSetSpaceProperties(spaceId, spaceProps);
        Map<String, String> pushedProps = pushedPropsCap.getValue();
        Assert.assertNotNull(pushedProps);
        Assert.assertEquals(2, pushedProps.size());
        Assert.assertNotNull(
            pushedProps.get(StorageProvider.PROPERTIES_SPACE_CREATED));
        Assert.assertEquals("two", pushedProps.get("one"));
    }

    @Test
    public void testGetAllSpaceProperties() throws Exception {
        EasyMock.expect(swiftClient.containerExists(spaceId)).andReturn(true);

        long spaceObjectCount = 5L;
        long spaceTotalSize = 500;
        ContainerMetadata mockMetadata =
            EasyMock.createMock("ContainerMetadata", ContainerMetadata.class);
        EasyMock.expect(mockMetadata.getCount())
                .andReturn(spaceObjectCount);
        EasyMock.expect(mockMetadata.getBytes())
                .andReturn(spaceTotalSize);
        Map<String, String> containerMetadata = new HashMap<>();
        containerMetadata.put("one", "two");
        EasyMock.expect(mockMetadata.getMetadata())
                .andReturn(containerMetadata);
        EasyMock.expect(swiftClient.getContainerMetadata(spaceId))
                .andReturn(mockMetadata);

        replayMocks();
        EasyMock.replay(mockMetadata);

        Map<String, String> spaceProps =
            provider.getAllSpaceProperties(spaceId);
        Assert.assertNotNull(spaceProps);
        Assert.assertEquals("two", spaceProps.get("one"));
        Assert.assertEquals(String.valueOf(spaceObjectCount),
                            spaceProps.get(
                                StorageProvider.PROPERTIES_SPACE_COUNT));
        Assert.assertEquals(String.valueOf(spaceTotalSize),
                            spaceProps.get(
                                StorageProvider.PROPERTIES_SPACE_SIZE));

        EasyMock.verify(mockMetadata);
    }

    /*
     * Implementation of OpenStackStorageProvider to permit testing
     */
    public class OpenStackTestProvider extends OpenStackStorageProvider {
        public OpenStackTestProvider(SwiftClient swiftClient) {
            super(swiftClient);
        }

        @Override
        public StorageProviderType getStorageProviderType() {
            return null;
        }

        @Override
        public String getAuthUrl() {
            return null;
        }

        @Override
        public String getProviderName() {
            return "Open Stack Test Provider";
        }
    }


}
