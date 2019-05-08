/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.duracloud.common.changenotifier.AccountChangeNotifier;
import org.duracloud.common.model.AclType;
import org.duracloud.common.rest.DuraCloudRequestContextUtil;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Andrew Woods
 * Date: 12/5/11
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

    private SecurityContextUtil securityContextUtil = new SecurityContextUtil();
    private AccountChangeNotifier notifier;
    private DuraCloudRequestContextUtil contextUtil;

    @Before
    public void setUp() throws Exception {
        mockProvider = createMockStorageProvider();
        context = createMock("SecurityContext", SecurityContext.class);
        contextUtil = createMock("DuraCloudRequestContextUtil", DuraCloudRequestContextUtil.class);
        notifier = createMock("AccountChangeNotifier", AccountChangeNotifier.class);

        authorities = new GrantedAuthority[] {new SimpleGrantedAuthority("ROLE_USER")};
        groups = new ArrayList<String>();
        groups.add(group);
    }

    @After
    public void tearDown() throws Exception {
        verify(mockProvider, context, contextUtil, notifier);
        SecurityContextHolder.clearContext();
    }

    private void replayMocks() {
        replay(mockProvider, context, contextUtil, notifier);
    }

    private StorageProvider createMockStorageProvider() {
        mockProvider = createMock("StorageProvider", StorageProvider.class);
        expect(mockProvider.getSpaces())
            .andReturn(allSpaces().iterator());

        for (String space : allSpaces()) {
            expect(mockProvider.getSpaceACLs(space))
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
        Authentication auth = createMock("Authentication", Authentication.class);
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
        expect(auth.getPrincipal()).andReturn(userDetails).anyTimes();
        replay(auth);
        expect(context.getAuthentication()).andReturn(auth).anyTimes();

        SecurityContextHolder.setContext(context);
    }

    @Test
    public void testCreateSpace() {
        createMockSecurityContext();

        String spaceId = spacePrefix + 2;
        mockProvider.createSpace(spaceId);
        expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = createProvider();
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

    protected ACLStorageProvider createProvider() {
        ACLStorageProvider provider =
            new ACLStorageProvider(mockProvider,
                                   securityContextUtil,
                                   notifier,
                                   contextUtil);
        return provider;
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
        expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = createProvider();
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
        expectLastCall().andThrow(new StorageException("test"));

        replayMocks();

        // verify original state
        ACLStorageProvider provider = createProvider();
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
