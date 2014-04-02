/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_ABSTAIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.common.model.AclType;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.FilterInvocation;
/**
 * @author Andrew Woods
 *         Date: Nov 18, 2011
 */
public class SpaceReadAccessVoterTest {

    private SpaceReadAccessVoter voter;

    private final String OPEN_SPACE_ID = "open-space";
    private Map<String, AclType> acls;
    private final String userRead = "username-r";
    private final String groupWrite = "group-curators-w";
    private final String storeId = "5";

    private StorageProviderFactory providerFactory;
    private UserDetailsService userDetailsService;
    private FilterInvocation resource;
    private HttpServletRequest request;
    private List<String> userPathOverrides;

    @Before
    public void setUp() {
        acls = new HashMap<String, AclType>();
        acls.put(PROPERTIES_SPACE_ACL + userRead, AclType.READ);
        acls.put(PROPERTIES_SPACE_ACL + groupWrite, AclType.WRITE);

        providerFactory = createStorageProviderFactoryMock();
        userDetailsService = createUserDetailsServiceMock();
        resource = EasyMock.createMock("FilterInvocation",
                                       FilterInvocation.class);
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);

        this.userPathOverrides = new LinkedList<String>();
        voter = new SpaceReadAccessVoter(providerFactory, userDetailsService, this.userPathOverrides);
    }

    @After
    public void tearDown() {
        EasyMock.verify(providerFactory, userDetailsService, resource, request);
    }

    private void replayMocks() {
        EasyMock.replay(providerFactory, userDetailsService, resource, request);
    }

    @Test
    public void testUserAccessMethodsGET() {
        boolean securedSpace = true;
        Authentication caller = registeredUser(userRead, "none");
        createMockInvocation(caller, securedSpace, HttpVerb.GET, 2);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_GRANTED, decision);
    }

    @Test
    public void testUserNoAccessMethodsGET() {
        boolean securedSpace = true;
        Authentication caller = registeredUser("joe", "none");
        createMockInvocation(caller, securedSpace, HttpVerb.GET,3);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testGroupAccessMethodsGET() {
        boolean securedSpace = true;
        Authentication caller = registeredUser("joe", groupWrite);
        createMockInvocation(caller, securedSpace, HttpVerb.GET, 2);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_GRANTED, decision);
    }



    @Test
    public void testGroupNoAccessMethodsGET() {
        boolean securedSpace = true;
        Authentication caller = registeredUser("joe", "none");
        createMockInvocation(caller, securedSpace, HttpVerb.GET,3);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testMethodsPUT() {
        boolean securedSpace = true;
        Authentication caller = registeredUser("joe", "none");
        createMockInvocation(caller, securedSpace, HttpVerb.PUT);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_ABSTAIN, decision);
    }

    @Test
    public void testVoteReservedResourcesOpenInit() {
        doTestVoteReservedResourcesOpen("init");
    }

    @Test
    public void testVoteReservedResourcesOpenSpaces() {
        doTestVoteReservedResourcesOpen("spaces");
    }

    @Test
    public void testVoteReservedResourcesOpenStores() {
        doTestVoteReservedResourcesOpen("stores");
    }

    @Test
    public void testVoteReservedResourcesOpenAcl() {
        doTestVoteReservedResourcesOpen("acl");
    }

    private void doTestVoteReservedResourcesOpen(String spaceId) {
        boolean securedSpace = false;
        Authentication caller = anonymousUser();
        createMockInvocation(spaceId, HttpVerb.GET);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_GRANTED, decision);
    }

    @Test
    public void testVoteUserPathOverridesReport() {
        boolean securedSpace = true;
        Authentication caller = registeredUser("joe", "none");
        createMockInvocation(caller, securedSpace, HttpVerb.GET, "/x-service-out/bit-integrity/report", 3);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        this.userPathOverrides.add("/x-service-out/bit-integrity.*");
        replayMocks();
        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_GRANTED, decision);
    }

    
    @Test
    public void testVoteOpenAuthenticated() {
        boolean securedSpace = false;
        verifyVote(registeredUser("joe", "none"), securedSpace, ACCESS_GRANTED);
    }

    @Test
    public void testVoteOpenAnonymous() {
        boolean securedSpace = false;
        verifyVote(anonymousUser(), securedSpace, ACCESS_GRANTED);
    }

    @Test
    public void testVoteClosedAuthenticatedNoAccess() {
        boolean securedSpace = true;
        verifyVote(registeredUser("joe", "none"), securedSpace, ACCESS_DENIED, 3);
    }

    @Test
    public void testVoteClosedAuthenticatedAccess() {
        boolean securedSpace = true;
        verifyVote(registeredUser(userRead, "x"), securedSpace, ACCESS_GRANTED);
    }

    @Test
    public void testVoteClosedAnonymous() {
        boolean securedSpace = true;
        verifyVote(anonymousUser(), securedSpace, ACCESS_DENIED);
    }

    private void verifyVote(Authentication caller,
                            boolean securedSpace,
                            int expected) {
        verifyVote(caller, securedSpace, expected, 2);
    }

    private void verifyVote(Authentication caller,
                            boolean securedSpace,
                            int expected,
                            int times) {
        resource = createMockInvocation(caller, securedSpace, HttpVerb.GET, times);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(expected, decision);
    }

    private Authentication registeredUser(String username, String group) {
        List<String> groups = new ArrayList<String>();
        groups.add(group);

        Collection<GrantedAuthority> authorities =
            Arrays.asList(new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_USER")});
        DuracloudUserDetails user = new DuracloudUserDetails(username,
                                                             "x",
                                                             "email",
                                                             true,
                                                             true,
                                                             true,
                                                             true,
                                                             authorities,
                                                             groups);
        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }

    private Authentication anonymousUser() {
        Collection<GrantedAuthority> authorities =
            Arrays.asList(new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_ANONYMOUS")});
        User user = new User("anon", "x", true, true, true, true, authorities);
        return new AnonymousAuthenticationToken("x", user, authorities);
    }

    private FilterInvocation createMockInvocation(Authentication caller,
                                      boolean securedSpace,
                                      HttpVerb method,
                                      int times) {
        return createMockInvocation(caller, securedSpace, method, null, times);
    }
    
    private FilterInvocation createMockInvocation(Authentication caller,
                                                  boolean securedSpace,
                                                  HttpVerb method) {
        return createMockInvocation(caller, securedSpace, method, 2);
    }

    private FilterInvocation createMockInvocation(Authentication caller,
                                                  boolean securedSpace,
                                                  HttpVerb method, String pathInfo, int times) {
        String spaceId = OPEN_SPACE_ID;
        if(pathInfo != null){
            spaceId = pathInfo;
        }else{
            if (securedSpace) {
                spaceId = "some-closed-space";
            }
        }

        EasyMock.expect(request.getMethod()).andReturn(method.name());

        if (method.isRead()) {
            EasyMock.expect(request.getQueryString()).andReturn(
                "storeID=" + storeId + "&attachment=true");
            EasyMock.expect(request.getPathInfo()).andReturn(spaceId).times(times);
        }

        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    private FilterInvocation createMockInvocation(String pathInfo,
                                                  HttpVerb method) {
        return createMockInvocation(pathInfo, method, 1);
    }

    private FilterInvocation createMockInvocation(String pathInfo,
                                                  HttpVerb method, int times) {
        EasyMock.expect(request.getMethod()).andReturn(method.name());
        EasyMock.expect(request.getPathInfo()).andReturn(pathInfo).times(times);
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    public Collection<ConfigAttribute> getConfigAttribute(boolean securedSpace) {
        if (securedSpace) {
            return createConfig(new SecurityConfig("ROLE_USER"));
        }
        return createConfig(new SecurityConfig("ROLE_ANONYMOUS"));
    }

    protected Collection<ConfigAttribute> createConfig(SecurityConfig role) {
        List<ConfigAttribute> configAttributes = new LinkedList<>();
        configAttributes.add(role);
        return configAttributes;
    }

    private UserDetailsService createUserDetailsServiceMock() {
        userDetailsService = EasyMock.createMock("UserDetailsService",
                                                 UserDetailsService.class);
        UserDetails userDetails = new User("username",
                                           "password",
                                           true,
                                           true,
                                           true,
                                           true,
                                           new LinkedList<GrantedAuthority>());

        EasyMock.expect(userDetailsService.loadUserByUsername(EasyMock.<String>anyObject()))
                .andReturn(userDetails)
                .anyTimes();

        return userDetailsService;
    }

    private StorageProviderFactory createStorageProviderFactoryMock() {
        providerFactory = EasyMock.createMock("StorageProviderFactory",
                                              StorageProviderFactory.class);

        StorageProvider provider = EasyMock.createMock("StorageProvider",
                                                       StorageProvider.class);

        EasyMock.expect(providerFactory.getStorageProvider(storeId)).andReturn(
            provider).anyTimes();
        EasyMock.expect(provider.getSpaceACLs(EasyMock.isA(String.class)))
                .andAnswer(getACLs());

        EasyMock.replay(provider);
        return providerFactory;
    }

    /**
     * This method returns 'open' or 'closed' base on the argument passed
     * to the getContentStore() call above.
     *
     * @return Map of ACLs
     */
    private IAnswer<? extends Map<String, AclType>> getACLs() {
        return new IAnswer<Map<String, AclType>>() {
            public Map<String, AclType> answer() throws Throwable {
                Object[] args = EasyMock.getCurrentArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(1, args.length);
                String arg = (String) args[0];
                if (arg.equals(OPEN_SPACE_ID)) {
                    acls.put(StorageProvider.PROPERTIES_SPACE_ACL_PUBLIC,
                             AclType.READ);
                }
                return acls;
            }
        };
    }

}
