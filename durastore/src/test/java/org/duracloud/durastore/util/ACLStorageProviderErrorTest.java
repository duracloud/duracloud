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
import org.duracloud.storage.error.StorageException;
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
 *         Date: 12/5/11
 */
public class ACLStorageProviderErrorTest {

    private StorageProvider mockProvider;
    private SecurityContext context;

    private String username = "username";
    private GrantedAuthority[] authorities;
    private List<String> groups;
    private static final String group = "group-a";
    private static final AclType aclType = AclType.READ;

    private static final String spacePrefix = "space-";
    private int NUM_SPACES = 5;


    @Before
    public void setUp() throws Exception {
        mockProvider = createMockStorageProvider();
        context = EasyMock.createMock("SecurityContext", SecurityContext.class);

        authorities = new GrantedAuthority[]{new GrantedAuthorityImpl(
            "ROLE_USER")};
        groups = new ArrayList<String>();
        groups.add(group);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(mockProvider, context);
        SecurityContextHolder.clearContext();
    }

    private void replayMocks() {
        EasyMock.replay(mockProvider, context);
    }

    private StorageProvider createMockStorageProvider() {
        mockProvider = EasyMock.createMock("StorageProvider",
                                           StorageProvider.class);
        EasyMock.expect(mockProvider.getSpaces())
                .andReturn(allSpaces().iterator());

        for (String space : allSpaces()) {
            EasyMock.expect(mockProvider.getSpaceACLs(space))
                    .andReturn(aclMap());
        }
        return mockProvider;
    }

    private Map<String, AclType> aclMap() {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        acls.put(PROPERTIES_SPACE_ACL + group, aclType);
        return acls;
    }

    private List<String> allSpaces() {
        List<String> spaces = new ArrayList<String>();
        for (int i = 0; i < NUM_SPACES; ++i) {
            spaces.add(spacePrefix + i);
        }
        return spaces;
    }

    private void createMockSecurityContext() {
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
        EasyMock.expect(auth.getPrincipal()).andReturn(userDetails).anyTimes();
        EasyMock.replay(auth);
        EasyMock.expect(context.getAuthentication()).andReturn(auth).anyTimes();

        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testCreateSpace() {
        createMockSecurityContext();

        String spaceId = spacePrefix + 2;
        mockProvider.createSpace(spaceId);
        EasyMock.expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = new ACLStorageProvider(mockProvider);
        verifyProviderState(provider);

        // method under test
        try {
            provider.createSpace(spaceId);
            Assert.fail("exception expected");

        } catch (StorageException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // verify state has not changed
        verifyProviderState(provider);
    }

    private void verifyProviderState(ACLStorageProvider provider) {
        Iterator<String> spaces = provider.getSpaces();
        List<String> allSpaces = allSpaces();
        int numSpaces = 0;
        while (spaces.hasNext()) {
            String space = spaces.next();
            Assert.assertTrue(space, allSpaces.contains(space));

            Map<String, AclType> acls = provider.getSpaceACLs(space);
            Assert.assertEquals(1, acls.size());
            Assert.assertEquals("acls: " + acls, acls.get(
                PROPERTIES_SPACE_ACL + group), aclType);

            numSpaces++;
        }
        Assert.assertEquals(allSpaces.size(), numSpaces);
    }

    @Test
    public void testDeleteSpace() {
        createMockSecurityContext();

        String spaceId = spacePrefix + 2;
        mockProvider.deleteSpace(spaceId);
        EasyMock.expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = new ACLStorageProvider(mockProvider);
        verifyProviderState(provider);

        // method under test
        try {
            provider.deleteSpace(spaceId);
            Assert.fail("exception expected");

        } catch (StorageException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // verify state has not changed
        verifyProviderState(provider);

    }

    @Test

    public void testSetSpaceACLs() {
        createMockSecurityContext();

        String spaceId = spacePrefix + 2;
        Map<String, AclType> acls = new HashMap<String, AclType>();
        acls.put(PROPERTIES_SPACE_ACL + "some-user", AclType.WRITE);
        mockProvider.setSpaceACLs(spaceId, acls);
        EasyMock.expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = new ACLStorageProvider(mockProvider);
        verifyProviderState(provider);

        // method under test
        try {
            provider.setSpaceACLs(spaceId, acls);
            Assert.fail("exception expected");

        } catch (StorageException e) {
            Assert.assertNotNull(e.getMessage());
        }

        // verify state has not changed
        verifyProviderState(provider);

    }

}
