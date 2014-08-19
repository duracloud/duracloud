/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.common.constant.Constants;
import org.duracloud.common.model.AclType;
import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.easymock.EasyMock;
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

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.duracloud.storage.provider.StorageProvider.PROPERTIES_SPACE_ACL;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_ABSTAIN;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.access.AccessDecisionVoter.ACCESS_GRANTED;

/**
 * @author Andrew Woods
 *         Date: Mar 19, 2010
 */
public class SpaceWriteAccessVoterTest {

    private SpaceWriteAccessVoter voter;

    private final String OPEN_SPACE_ID = "open-space";
    private Map<String, AclType> acls;
    private final String groupWrite = "group-curators-w";
    private final String storeId = "5";

    private StorageProviderFactory providerFactory;
    private StorageProvider provider;
    private UserDetailsService userDetailsService;
    private FilterInvocation resource;
    private HttpServletRequest request;
    private List<StorageAccount> accounts;

    @Before
    public void setUp() {
        acls = new HashMap<String, AclType>();
        acls.put(PROPERTIES_SPACE_ACL + LOGIN.USER_READ.name(), AclType.READ);
        acls.put(PROPERTIES_SPACE_ACL + LOGIN.USER_WRITE.name(), AclType.WRITE);
        acls.put(PROPERTIES_SPACE_ACL + groupWrite, AclType.WRITE);

        accounts = new LinkedList<StorageAccount>();

        providerFactory = createStorageProviderFactoryMock();
        userDetailsService = EasyMock.createMock("UserDetailsService",
                                                 UserDetailsService.class);
        resource = EasyMock.createMock("FilterInvocation",
                                       FilterInvocation.class);
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);
        voter = new SpaceWriteAccessVoter(providerFactory, userDetailsService);
        
    }

    protected void expectGetSpaceAcls() {
        EasyMock.expect(provider.getSpaceACLs(EasyMock.isA(String.class))).andReturn(acls);
    }

    @After
    public void tearDown() {
        EasyMock.verify(userDetailsService, resource, request, providerFactory,provider);
    }

    private void replayMocks() {
        EasyMock.replay(userDetailsService, resource, request, providerFactory,provider);
    }

    @Test
    public void testUserReadAccessMethodsPUTContent() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        String contentId = "/content-id";
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(HttpVerb.PUT, contentId);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        expectGetSpaceAcls();

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testUserReadAccessMethodsPUTSpace() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testAdminPUTSpace() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.ADMIN;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_GRANTED, decision);
    }

    @Test
    public void testUserWriteAccessMethodsSpacePUT() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_WRITE;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testUserNoAccessMethodsPUTContent() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        String contentId = "/content-id";
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(HttpVerb.PUT, contentId);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        expectGetSpaceAcls();

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testUserNoAccessMethodsPUTSpace() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testGroupAccessMethodsPUT() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        Authentication caller = registeredUser(login, groupWrite);
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testGroupNoAccessMethodsPUT() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.PUT);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_DENIED, decision);
    }

    @Test
    public void testUserAclPOST() {
        LOGIN login = LOGIN.USER_WRITE;
        int decision = ACCESS_DENIED;
        doTestAclPUT(login, decision);
    }

    @Test
    public void testAdminAclPOST() {
        LOGIN login = LOGIN.ADMIN;
        int decision = ACCESS_GRANTED;
        doTestAclPUT(login, decision);
    }

    @Test
    public void testSnapshotInProgress() {
        LOGIN login = LOGIN.ADMIN;
        int expectedDecision = ACCESS_DENIED;
        boolean securedSpace = true;
        Authentication caller = registeredUser(login, "none");
        
        EasyMock.expect(request.getPathInfo()).andReturn("test").times(1);

        EasyMock.expect(request.getMethod()).andReturn(HttpVerb.POST.name());
            
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        addGetQueryStringInvocation(1);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        StorageAccount account =
            new StorageAccountImpl(storeId,
                                   "test",
                                   "test",
                                   StorageProviderType.SNAPSHOT);
        accounts.add(account);

        Map<String, String> spaceProps = new HashMap<>();
        spaceProps.put(Constants.SNAPSHOT_ID_PROP, "snapshot-id");
        EasyMock.expect(provider.getSpaceProperties(EasyMock.isA(String.class)))
                .andReturn(spaceProps);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(expectedDecision, decision);
    }

    private void doTestAclPUT(LOGIN login, int expectedDecision) {
        boolean securedSpace = true;
        Authentication caller = registeredUser(login, "none");
        createMockUpdateAclInvocation(login);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(expectedDecision, decision);
    }

    @Test
    public void testMethodsGET() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        Authentication caller = registeredUser(login, "none");
        createMockInvocation(securedSpace, login, HttpVerb.GET);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);

        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(ACCESS_ABSTAIN, decision);
    }

    @Test
    public void testUserSpaceDELETE() {
        LOGIN login = LOGIN.USER_WRITE;
        doTestSpaceDELETE(login, ACCESS_DENIED);
    }

    @Test
    public void testAdminSpaceDELETE() {
        LOGIN login = LOGIN.ADMIN;
        doTestSpaceDELETE(login, ACCESS_GRANTED);
    }

    private void doTestSpaceDELETE(LOGIN login, int expectedDecision) {
        boolean securedSpace = false;
        Authentication caller = registeredUser(login, "none");
        createMockDeleteSpaceInvocation(login);
        createUserDetailsServiceMock(login);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        addGetQueryStringInvocation(1);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(expectedDecision, decision);
    }

    @Test
    public void testVoteOpenAuthenticated() {
        boolean securedSpace = false;
        LOGIN login = LOGIN.USER_READ;
        createUserDetailsServiceMock(login);
        expectGetSpaceAcls();
        addGetQueryStringInvocation(1);
        verifyVote(registeredUser(login, "none"), securedSpace, ACCESS_DENIED);
    }

    @Test
    public void testVoteOpenAnonymous() {
        boolean securedSpace = false;
        verifyVote(anonymousUser(), securedSpace, ACCESS_DENIED);
    }

    @Test
    public void testVoteClosedAuthenticatedNoAccess() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        createUserDetailsServiceMock(login);
        expectGetSpaceAcls();
        addGetQueryStringInvocation(1);
        verifyVote(registeredUser(login, "none"), securedSpace, ACCESS_DENIED);
    }

    @Test
    public void testVoteClosedAuthenticatedAccess() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.USER_READ;
        createUserDetailsServiceMock(login);
        addGetQueryStringInvocation(1);
        expectGetSpaceAcls();
        verifyVote(registeredUser(login, "no"), securedSpace, ACCESS_DENIED);
    }

    @Test
    public void testVoteClosedAnonymous() {
        boolean securedSpace = true;
        verifyVote(anonymousUser(), securedSpace, ACCESS_DENIED);
    }

    @Test
    public void testVoteRoot() {
        boolean securedSpace = true;
        LOGIN login = LOGIN.ROOT;
        Authentication auth = registeredUser(login, "none");
        verifyVote(auth, securedSpace, ACCESS_GRANTED);
    }

    private void verifyVote(Authentication caller,
                            boolean securedSpace,
                            int expected) {
        createMockInvocationVerifyVote(caller, securedSpace, HttpVerb.POST);
        Collection<ConfigAttribute> config = getConfigAttribute(securedSpace);
        replayMocks();

        int decision = voter.vote(caller, resource, config);
        Assert.assertEquals(expected, decision);
    }

    private Authentication registeredUser(LOGIN login, String group) {
        List<String> groups = new ArrayList<String>();
        groups.add(group);

        DuracloudUserDetails user = new DuracloudUserDetails(login.name(),
                                                             "x",
                                                             "email",
                                                             true,
                                                             true,
                                                             true,
                                                             true,
                                                             login.auths,
                                                             groups);
        return new UsernamePasswordAuthenticationToken(user, "", login.auths);
    }

    private Authentication anonymousUser() {
        Collection<GrantedAuthority> authorities =
            Arrays.asList(new GrantedAuthority[]{new GrantedAuthorityImpl("ROLE_ANONYMOUS")});
        User user = new User("anon", "x", true, true, true, true, authorities);
        return new AnonymousAuthenticationToken("x", user, authorities);
    }


    private FilterInvocation createMockInvocation(boolean securedSpace,
                                                  LOGIN login,
                                                  HttpVerb method) {
        String spaceId = OPEN_SPACE_ID;
        if (securedSpace) {
            spaceId = "some-closed-space";
        }

        int times;
        if (method.isRead() || login.equals(LOGIN.ADMIN)) {
            times = 1;
        } else {
            times = 2;
            EasyMock.expect(request.getPathInfo()).andReturn(spaceId).times(2);
        }

        EasyMock.expect(request.getMethod()).andReturn(method.name()).times(
            times);

        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    private FilterInvocation createMockInvocationVerifyVote(Authentication caller,
                                                            boolean securedSpace,
                                                            HttpVerb method) {
        String spaceId = OPEN_SPACE_ID;
        if (securedSpace) {
            spaceId = "some-closed-space";
        }

        int times = 1;
        
        boolean root = false;
        for(GrantedAuthority a : caller.getAuthorities()){
            if(a.getAuthority().equals("ROLE_ROOT")){
                root = true;
            }
        }
        
        if (!(caller instanceof AnonymousAuthenticationToken) && !root) {
            times = 4;

            EasyMock.expect(request.getQueryString()).andReturn(
                "storeID=" + storeId + "&attachment=true");
            EasyMock.expect(request.getPathInfo()).andReturn(spaceId).times(2);
        }

        EasyMock.expect(request.getMethod()).andReturn(method.name()).times(
            times);

        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    private FilterInvocation createMockUpdateAclInvocation(LOGIN login) {
        String path = "/acl/" + OPEN_SPACE_ID;

        if (login.equals(LOGIN.ADMIN)) {
            EasyMock.expect(request.getMethod()).andReturn(HttpVerb.POST
                                                               .name());

        } else {
            EasyMock.expect(request.getPathInfo()).andReturn(path);
            EasyMock.expect(request.getMethod())
                    .andReturn(HttpVerb.POST.name())
                    .times(4);
        }
        
        addGetQueryStringInvocation(1);
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    private FilterInvocation createMockDeleteSpaceInvocation(LOGIN login) {
        String spaceId = OPEN_SPACE_ID;

        if (login.equals(LOGIN.ADMIN)) {
            EasyMock.expect(request.getMethod()).andReturn(HttpVerb.DELETE
                                                               .name());

        } else {
            EasyMock.expect(request.getPathInfo()).andReturn(spaceId).times(2);
            EasyMock.expect(request.getMethod())
                    .andReturn(HttpVerb.DELETE.name())
                    .times(3);
        }
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    private FilterInvocation createMockInvocation(HttpVerb method,
                                                  String contentId) {
        String path = OPEN_SPACE_ID + contentId;

        
        addGetQueryStringInvocation(2);
        EasyMock.expect(request.getPathInfo()).andReturn(path).times(3);
        EasyMock.expect(request.getMethod()).andReturn(method.name()).times(4);
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }

    protected void addGetQueryStringInvocation(int times) {
        EasyMock.expect(request.getQueryString()).andReturn(
            "storeID=" + storeId + "&attachment=true").times(times);
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



    private UserDetailsService createUserDetailsServiceMock(LOGIN login) {
        UserDetails userDetails = new User(login.name(),
                                           "password",
                                           true,
                                           true,
                                           true,
                                           true,
                                           login.auths);

        EasyMock.expect(userDetailsService.loadUserByUsername(EasyMock.<String>anyObject()))
                .andReturn(userDetails)
                .anyTimes();

        return userDetailsService;
    }

    private StorageProviderFactory createStorageProviderFactoryMock() {
        providerFactory = EasyMock.createMock("StorageProviderFactory",
                                              StorageProviderFactory.class);

        provider = EasyMock.createMock("StorageProvider",
                                                       StorageProvider.class);

        EasyMock.expect(providerFactory.getStorageProvider(storeId)).andReturn(
            provider).anyTimes();
        
        EasyMock.expect(providerFactory.getStorageAccounts()).andReturn(accounts).anyTimes();


        return providerFactory;
    }

    private enum LOGIN {
        USER_READ("ROLE_USER"),
        USER_WRITE("ROLE_USER"),
        ROOT("ROLE_ROOT"),
        ADMIN("ROLE_ADMIN");

        private Collection<GrantedAuthority> auths;

        LOGIN(String role) {
            auths = Arrays.asList(new GrantedAuthority[]{new GrantedAuthorityImpl(role)});
        }
    }

}
