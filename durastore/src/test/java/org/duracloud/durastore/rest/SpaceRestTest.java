/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import org.duracloud.common.model.AclType;
import org.duracloud.durastore.error.ResourceException;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 11/15/11
 */
public class SpaceRestTest {

    private SpaceRest spaceRest;
    private SpaceResource spaceResource;
    private SecurityContextUtil securityContextUtil;
    private HttpHeaders httpHeaders;
    private MultivaluedMap<String, String> headersMap;

    private final static String spaceId = "space-id";
    private final static String storeId = "store-id";
    private static Map<String, String> spaceProps;
    private static Map<String, AclType> spaceACLs;

    private final static String user0 = "user-0";
    private final static String user1 = "user-1";
    private final static String group0 = "group-0";

    private final static String prefix = BaseRest.HEADER_PREFIX;
    private final static String mimeHeader =
        StorageProvider.PROPERTIES_CONTENT_MIMETYPE;
    private final static String aclHeader =
        StorageProvider.PROPERTIES_SPACE_ACL;


    @BeforeClass
    public static void beforeClass() {
        spaceACLs = new HashMap<String, AclType>();
        spaceACLs.put(aclHeader + user0, AclType.READ);
        spaceACLs.put(aclHeader + group0, AclType.READ);
        spaceACLs.put(aclHeader + user1, AclType.WRITE);

        spaceProps = new HashMap<String, String>();
        spaceProps.put("unknown-name", "unknown-value");
        spaceProps.put(mimeHeader, "text/plain");
        for (String key : spaceACLs.keySet()) {
            spaceProps.put(key, spaceACLs.get(key).name());
        }
    }

    @Before
    public void setUp() throws Exception {
        httpHeaders = EasyMock.createMock("HttpHeaders", HttpHeaders.class);
        headersMap = EasyMock.createMock("MultivaluedMap",
                                         MultivaluedMap.class);
        spaceResource = EasyMock.createMock("SpaceResource",
                                            SpaceResource.class);
        securityContextUtil = EasyMock.createMock("SecurityContextUtil",
                                                  SecurityContextUtil.class);

        spaceRest = new SpaceRest(spaceResource, securityContextUtil);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(httpHeaders,
                        headersMap,
                        spaceResource,
                        securityContextUtil);
    }

    private void replayMocks() {
        EasyMock.replay(httpHeaders,
                        headersMap,
                        spaceResource,
                        securityContextUtil);
    }

    @Test
    public void testGetSpaceProperties() throws Exception {
        createGetSpacePropertiesMocks();

        Response response = spaceRest.getSpaceProperties(spaceId, storeId);
        Assert.assertNotNull(response);

        MultivaluedMap metadata = response.getMetadata();
        Assert.assertNotNull(metadata);
        Assert.assertEquals(spaceProps.size(), metadata.size());
        for (String propName : spaceProps.keySet()) {
            String metaName = prefix + propName;
            Assert.assertTrue(metadata.containsKey(metaName));
            Assert.assertEquals(spaceProps.get(propName), metadata.getFirst(
                metaName));
        }
    }

    private void createGetSpacePropertiesMocks() throws ResourceException {
        EasyMock.expect(spaceResource.getSpaceProperties(spaceId, storeId))
                .andReturn(spaceProps);
        replayMocks();
    }

    @Test
    public void testGetSpaceACLs() throws Exception {
        createGetSpaceACLsMocks();

        Response response = spaceRest.getSpaceACLs(spaceId, storeId);
        Assert.assertNotNull(response);

        MultivaluedMap metadata = response.getMetadata();
        Assert.assertNotNull(metadata);
        Assert.assertEquals(spaceACLs.size(), metadata.size());
        for (String aclName : spaceACLs.keySet()) {
            String metaName = prefix + aclName;
            Assert.assertTrue(metadata.containsKey(metaName));
            Assert.assertEquals(spaceProps.get(aclName), metadata.getFirst(
                metaName));
        }
    }

    private void createGetSpaceACLsMocks() throws ResourceException {
        EasyMock.expect(spaceResource.getSpaceACLs(spaceId, storeId)).andReturn(
            spaceACLs);
        replayMocks();
    }

    @Test
    public void testUpdateSpaceACLs() throws Exception {
        createUpdateSpaceACLsMocks();

        Response response = spaceRest.updateSpaceACLs(spaceId, storeId);
        Assert.assertNotNull(response);
        Assert.assertEquals(Response.Status.OK.getStatusCode(),
                            response.getStatus());
    }

    private void createUpdateSpaceACLsMocks() throws ResourceException {
        // header mocks
        spaceRest.headers = httpHeaders;

        EasyMock.expect(httpHeaders.getRequestHeaders()).andReturn(headersMap);
        Map<String, String> spaceAclProps = new HashMap<String, String>();
        for (String key : spaceACLs.keySet()) {
            spaceAclProps.put(key, spaceACLs.get(key).name());
        }
        doCreateUpdatePropertiesMocks(spaceAclProps);

        // space resource mocks
        spaceResource.updateSpaceACLs(spaceId, spaceACLs, storeId);
        EasyMock.expectLastCall();

        replayMocks();
    }

    private void doCreateUpdatePropertiesMocks(Map<String, String> map)
        throws ResourceException {
        Map<String, String> propMap = new HashMap<String, String>();
        for (String key : map.keySet()) {
            propMap.put(prefix + key, map.get(key));
        }

        Set<String> propKeys = propMap.keySet();
        EasyMock.expect(headersMap.keySet()).andReturn(propKeys);
        for (String propName : propKeys) {
            EasyMock.expect(headersMap.getFirst(propName))
                    .andReturn(propMap.get(propName));
        }
    }

}
