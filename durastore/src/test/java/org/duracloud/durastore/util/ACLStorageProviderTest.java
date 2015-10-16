/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.common.model.AclType;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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

        createMockStorageProvider(1);
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
        createMockStorageProvider(1);
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

    private void createMockStorageProvider(int times) {
        mockProvider = EasyMock.createMock("StorageProvider",
                                           StorageProvider.class);
        for (int i = 0; i < times; ++i) {
            EasyMock.expect(mockProvider.getSpaces())
                    .andReturn(allSpaces().iterator());
        }

        for (String space : allSpaces()) {
            Map<String, AclType> acls = new HashMap<String, AclType>();
            if (space.equals(spacePrefix + 1)) {
                acls.put(StorageProvider.PROPERTIES_SPACE_ACL_PUBLIC,
                         AclType.READ);
            }

            if (space.equals(spacePrefix + 2)) {
                acls.put(PROPERTIES_SPACE_ACL + username, AclType.READ);

            } else if (space.equals(spacePrefix + 3)) {
                acls.put(PROPERTIES_SPACE_ACL + groupA, AclType.WRITE);
            }
            EasyMock.expect(mockProvider.getSpaceACLs(space))
                    .andReturn(acls)
                    .times(times);
        }
    }

    private void createMockSecurityContext(int times) {
        Authentication auth = EasyMock.createMock("Authentication",
                                                  Authentication.class);
        DuracloudUserDetails userDetails = new DuracloudUserDetails(username,
                                                                    "password",
                                                                    "email",
                                                                    "",
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    true,
                                                                    Arrays.asList(authorities),
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

    private Map<String, String> createSpaceProperties() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("name0", "value0");
        props.put("name1", "value1");
        return props;
    }

    @Test
    public void testGetSpaceACLs() throws Exception {
        String spaceId = spacePrefix + 4;
        createMockStorageProvider(1);
        Map<String, AclType> origAcls = createSpaceACLs();

        mockProvider.setSpaceACLs(spaceId, origAcls);
        EasyMock.expectLastCall();
        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        Map<String, AclType> acls = provider.getSpaceACLs(spaceId);
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

    private Map<String, AclType> createSpaceACLs() {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        acls.put(PROPERTIES_SPACE_ACL + username, AclType.WRITE);
        return acls;
    }

    @Test
    public void testSetSpaceACLs() throws Exception {
        String spaceId = spacePrefix + 2;
        createMockStorageProvider(1);
        Map<String, AclType> origAcls = createSpaceACLs();

        mockProvider.setSpaceACLs(spaceId, origAcls);
        EasyMock.expectLastCall();

        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        provider.setSpaceACLs(spaceId, origAcls);

        // getting ACLs should only hit the cache.
        Map<String, AclType> acls = provider.getSpaceACLs(spaceId);
        Assert.assertNotNull(acls);
        Assert.assertEquals(origAcls, acls);
    }

    @Test
    public void testClearCache() throws InterruptedException {
        String spaceId = "ACLStorageProvider-cache";
        createMockStorageProvider(2);

        EasyMock.expect(mockProvider.getSpaceACLs(spaceId))
                .andReturn(new HashMap<String, AclType>());
        mockProvider.deleteSpace(spaceId);
        EasyMock.expectLastCall();

        replayMocks();

        // method under test
        provider = new ACLStorageProvider(mockProvider);
        provider.deleteSpace(spaceId);

        // wait for cache to be cleared and reloaded.
        provider.getSpaceACLs(spaceId);
    }

}
