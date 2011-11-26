/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.provider.StorageProvider.AccessType.CLOSED;
import static org.duracloud.storage.provider.StorageProvider.AccessType.OPEN;
import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;

/**
 * @author Andrew Woods
 *         Date: 11/22/11
 */
public class ACLStorageProviderTest {

    private ACLStorageProvider provider;
    private StorageProvider mockProvider;

    private SecurityContext context;

    private String username = "username";
    private GrantedAuthority[] authorities;
    private List<String> groups;
    private static final String groupA = "group-a";

    private static final String spacePrefix = "space-";
    private int NUM_SPACES = 5;


    @Before
    public void setUp() throws Exception {
        mockProvider = createMockStorageProvider();
        context = EasyMock.createMock("SecurityContext", SecurityContext.class);

        authorities = new GrantedAuthority[]{new GrantedAuthorityImpl(
            "ROLE_USER")};
        groups = new ArrayList<String>();
        groups.add(groupA);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(mockProvider, context);
        SecurityContextHolder.clearContext();
    }

    private void replayMocks() {
        EasyMock.replay(mockProvider, context);
    }

    @Test
    public void testCreateSpace() {
        String spaceId = spacePrefix + 16;

        createMockSecurityContext(3);

        mockProvider.createSpace(spaceId);
        EasyMock.expectLastCall();

        replayMocks();

        // load cache
        provider = new ACLStorageProvider(mockProvider);
        Iterator<String> spaces = provider.getSpaces();
        Assert.assertNotNull(spaces);

        // method under test
        provider.createSpace(spaceId);

        spaces = provider.getSpaces();
        Assert.assertNotNull(spaces);

        int i = 0;
        List<String> userSpacesA = new ArrayList<String>();
        userSpacesA.add(spacePrefix + 1);
        userSpacesA.add(spacePrefix + 2);
        userSpacesA.add(spacePrefix + 3);
        userSpacesA.add(spaceId);

        while (spaces.hasNext()) {
            String space = spaces.next();
            Assert.assertTrue(userSpacesA.contains(space));
            i++;
        }
        Assert.assertEquals(userSpacesA.size(), i);
    }

    @Test
    public void testGetSpaces() throws Exception {
        createMockSecurityContext(1);
        replayMocks();

        List<String> userSpacesA = new ArrayList<String>();
        userSpacesA.add(spacePrefix + 1);
        userSpacesA.add(spacePrefix + 2);
        userSpacesA.add(spacePrefix + 3);

        provider = new ACLStorageProvider(mockProvider);
        Iterator<String> spaces = provider.getSpaces();
        Assert.assertNotNull(spaces);

        int i = 0;
        while (spaces.hasNext()) {
            String space = spaces.next();
            Assert.assertTrue(userSpacesA.contains(space));
            i++;
        }
        Assert.assertEquals(userSpacesA.size(), i);
    }

    private StorageProvider createMockStorageProvider() {
        mockProvider = EasyMock.createMock("StorageProvider",
                                           StorageProvider.class);
        EasyMock.expect(mockProvider.getSpaces())
                .andReturn(allSpaces().iterator());

        for (String space : allSpaces()) {
            StorageProvider.AccessType access = CLOSED;
            if (space.equals(spacePrefix + 1)) {
                access = OPEN;
            }
            EasyMock.expect(mockProvider.getSpaceAccess(space))
                    .andReturn(access);

            Map<String, String> acls = new HashMap<String, String>();
            if (space.equals(spacePrefix + 2)) {
                acls.put(PROPERTIES_SPACE_ACL + username, "r");

            } else if (space.equals(spacePrefix + 3)) {
                acls.put(PROPERTIES_SPACE_ACL + groupA, "w");
            }
            EasyMock.expect(mockProvider.getSpaceACLs(space)).andReturn(acls);
        }
        return mockProvider;
    }

    private void createMockSecurityContext(int times) {
        Authentication auth = EasyMock.createMock("Authentication",
                                                  Authentication.class);
        DuracloudUserDetails userDetails = new DuracloudUserDetails(username,
                                                                    "password",
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    authorities,
                                                                    groups);
        EasyMock.expect(auth.getPrincipal())
                .andReturn(userDetails)
                .times(times);
        EasyMock.replay(auth);
        EasyMock.expect(context.getAuthentication())
                .andReturn(auth)
                .times(times);

        SecurityContextHolder.setContext(context);
    }

    private List<String> allSpaces() {
        List<String> spaces = new ArrayList<String>();
        for (int i = 0; i < NUM_SPACES; ++i) {
            spaces.add(spacePrefix + i);
        }
        return spaces;
    }

    @Test
    public void testSetSpaceProperties() throws Exception {
        String spaceId = spacePrefix + 1;
        Map<String, String> properties = createSpaceProperties();

        StorageProvider.AccessType origAccess = OPEN;
        mockProvider.setSpaceProperties(spaceId, properties);
        EasyMock.expectLastCall();
        replayMocks();

        provider = new ACLStorageProvider(mockProvider);
        StorageProvider.AccessType access = provider.getSpaceAccess(spaceId);
        Assert.assertNotNull(access);
        Assert.assertEquals(origAccess, access);

        // method under test
        properties.put(StorageProvider.PROPERTIES_SPACE_ACCESS, CLOSED.name());
        provider.setSpaceProperties(spaceId, properties);

        // verify side effects
        access = provider.getSpaceAccess(spaceId);
        Assert.assertNotNull(access);
        Assert.assertEquals(CLOSED, access);
        Assert.assertNotSame(origAccess, access);
    }

    private Map<String, String> createSpaceProperties() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("name0", "value0");
        props.put("name1", "value1");
        return props;
    }

    @Test
    public void testGetSpaceACLs() throws Exception {
        String spaceId = spacePrefix + 1;
        Map<String, String> origAcls = createSpaceACLs();

        mockProvider.setSpaceACLs(spaceId, origAcls);
        EasyMock.expectLastCall();
        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        Map<String, String> acls = provider.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);
        Assert.assertEquals(new HashMap<String, String>(), acls);

        provider.setSpaceACLs(spaceId, origAcls);
        acls = provider.getSpaceACLs(spaceId);
        Assert.assertEquals(origAcls, acls);

        // calling a second time should result in hitting the cache.
        acls = provider.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);
        Assert.assertEquals(origAcls, acls);
    }

    private Map<String, String> createSpaceACLs() {
        Map<String, String> acls = new HashMap<String, String>();
        acls.put(PROPERTIES_SPACE_ACL + username, "w");
        return acls;
    }

    @Test
    public void testSetSpaceACLs() throws Exception {
        String spaceId = spacePrefix + 2;
        Map<String, String> origAcls = createSpaceACLs();

        mockProvider.setSpaceACLs(spaceId, origAcls);
        EasyMock.expectLastCall();

        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        provider.setSpaceACLs(spaceId, origAcls);

        // getting ACLs should only hit the cache.
        Map<String, String> acls = provider.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);
        Assert.assertEquals(origAcls, acls);
    }

    @Test
    public void testGetSpaceAccess() throws Exception {
        String spaceId = spacePrefix + 1;
        StorageProvider.AccessType origAccess = OPEN;
        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        StorageProvider.AccessType access = provider.getSpaceAccess(spaceId);
        Assert.assertNotNull(access);
        Assert.assertEquals(origAccess, access);

        // calling a second time should result in hitting the cache.
        access = provider.getSpaceAccess(spaceId);
        Assert.assertNotNull(access);
        Assert.assertEquals(origAccess, access);
    }

    @Test
    public void testSetSpaceAccess() throws Exception {
        String spaceId = spacePrefix + 4;
        StorageProvider.AccessType origAccess = OPEN;

        mockProvider.setSpaceAccess(spaceId, origAccess);
        EasyMock.expectLastCall();

        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        provider.setSpaceAccess(spaceId, origAccess);

        // getting access should only hit the cache.
        StorageProvider.AccessType access = provider.getSpaceAccess(spaceId);
        Assert.assertNotNull(access);
        Assert.assertEquals(origAccess, access);
    }

}
