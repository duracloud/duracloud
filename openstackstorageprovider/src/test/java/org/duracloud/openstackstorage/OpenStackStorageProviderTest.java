/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.openstackstorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesContainerInfo;
import com.rackspacecloud.client.cloudfiles.FilesObject;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jclouds.openstack.swift.SwiftClient;
import org.jclouds.openstack.swift.domain.ContainerMetadata;
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
    private FilesClient filesClient;
    private SwiftClient swiftClient;
    private String spaceId = "space-id";

    @Before
    public void setUp() throws Exception {
        filesClient = EasyMock.createMock("FilesClient", FilesClient.class);
        swiftClient = EasyMock.createMock("SwiftClient", SwiftClient.class);
        provider = new OpenStackTestProvider(filesClient, swiftClient);

        EasyMock.expect(filesClient.containerExists(EasyMock.isA(String.class)))
                .andReturn(true)
                .anyTimes();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(filesClient, swiftClient);
    }

    private void replayMocks() {
        EasyMock.replay(filesClient, swiftClient);
    }

    @Test
    public void testGetSpaceContentsChunked() throws Exception {
        String prefix = null;
        long maxResults = 2;
        String marker = null;

        List<FilesObject> filesObjectList = new ArrayList<>();
        EasyMock.expect(filesClient.listObjects(spaceId,
                                                prefix,
                                                new Long(maxResults).intValue(),
                                                marker))
                .andReturn(filesObjectList);

        replayMocks();

        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   prefix,
                                                                   maxResults,
                                                                   marker);
        Assert.assertNotNull(contentIds);
    }

    @Test
    public void testSetSpaceProperties() {
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
        ContainerMetadata mockMetadata =
            EasyMock.createMock("ContainerMetadata", ContainerMetadata.class);

        Map<String, String> containerMetadata = new HashMap<>();
        containerMetadata.put("one", "two");
        EasyMock.expect(mockMetadata.getMetadata())
                .andReturn(containerMetadata);
        EasyMock.expect(swiftClient.getContainerMetadata(spaceId))
                .andReturn(mockMetadata);

        FilesContainerInfo containerInfo =
            EasyMock.createMock("FilesContainerInfo", FilesContainerInfo.class);

        int spaceObjectCount = 5;
        long spaceTotalSize = 500;
        EasyMock.expect(filesClient.getContainerInfo(spaceId))
                .andReturn(containerInfo);
        EasyMock.expect(containerInfo.getObjectCount())
                .andReturn(spaceObjectCount);
        EasyMock.expect(containerInfo.getTotalSize())
                .andReturn(spaceTotalSize);

        replayMocks();
        EasyMock.replay(mockMetadata, containerInfo);

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

        EasyMock.verify(mockMetadata, containerInfo);
    }

    /*
     * Implementation of OpenStackStorageProvider to permit testing
     */
    public class OpenStackTestProvider extends OpenStackStorageProvider {
        public OpenStackTestProvider(FilesClient filesClient,
                                     SwiftClient swiftClient) {
            super(filesClient, swiftClient);
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
