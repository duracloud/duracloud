/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import static org.easymock.EasyMock.createMock;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.SecurityConfig;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.User;
import static org.springframework.security.vote.AccessDecisionVoter.ACCESS_DENIED;
import static org.springframework.security.vote.AccessDecisionVoter.ACCESS_GRANTED;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Andrew Woods
 *         Date: Mar 19, 2010
 */
public class SpaceAccessVoterTest {

    private SpaceAccessVoter voter;
    private final String OPEN_SPACE_ID = "open-space";

    @Before
    public void setUp() {
        voter = new SpaceAccessVoterMockImpl();
    }

    @After
    public void tearDown() {
        voter = null;
    }

    @Test
    public void testVoteOpen() {
        boolean securedSpace = false;
        verifyVote(registeredUser(), securedSpace, ACCESS_GRANTED);
        verifyVote(anonymousUser(), securedSpace, ACCESS_GRANTED);
    }

    @Test
    public void testVoteClosed() {
        boolean securedSpace = true;
        verifyVote(registeredUser(), securedSpace, ACCESS_GRANTED);
        verifyVote(anonymousUser(), securedSpace, ACCESS_DENIED);
    }

    private void verifyVote(Authentication auth,
                            boolean securedSpace,
                            int expected) {
        Object resource;
        ConfigAttributeDefinition config;
        resource = getMockInvocation(securedSpace);
        config = getConfigAttribute(securedSpace);

        int decision = voter.vote(auth, resource, config);
        Assert.assertEquals(expected, decision);
    }

    private Authentication registeredUser() {
        GrantedAuthority[] authorities = new GrantedAuthorityImpl[]{new GrantedAuthorityImpl(
            "ROLE_USER")};
        User user = new User("user", "x", true, true, true, true, authorities);
        return new UsernamePasswordAuthenticationToken(user, "", authorities);
    }

    private Authentication anonymousUser() {
        GrantedAuthority[] authorities = new GrantedAuthorityImpl[]{new GrantedAuthorityImpl(
            "ROLE_ANONYMOUS")};
        User user = new User("anon", "x", true, true, true, true, authorities);
        return new AnonymousAuthenticationToken("x", user, authorities);
    }

    private Object getMockInvocation(boolean securedSpace) {
        HttpServletRequest request = createMock(HttpServletRequest.class);

        String spaceId = OPEN_SPACE_ID;
        if (securedSpace) {
            spaceId = "some-closed-space";
        }

        EasyMock.expect(request.getServletPath()).andReturn(
            "/" + spaceId + "/some-objId");
        EasyMock.expect(request.getPathInfo()).andReturn(spaceId);
        EasyMock.expect(request.getLocalPort()).andReturn(8080);
        EasyMock.expect(request.getQueryString()).andReturn(
            "storeID=5&attachment=true");

        EasyMock.replay(request);

        FilterInvocation resource = EasyMock.createMock(FilterInvocation.class);
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        EasyMock.replay(resource);
        return resource;
    }

    public ConfigAttributeDefinition getConfigAttribute(boolean securedSpace) {
        if (securedSpace) {
            return new ConfigAttributeDefinition(new SecurityConfig("ROLE_USER"));
        }
        return new ConfigAttributeDefinition(new SecurityConfig("ROLE_ANONYMOUS"));
    }

    /**
     * This class mocks out the ContentStore of the SpaceAccessVoter.
     * Otherwise, it uses all of the SpaceAccessVoter logic.
     */
    private class SpaceAccessVoterMockImpl extends SpaceAccessVoter {
        protected ContentStore getContentStore(String host,
                                               String port,
                                               String storeId) {
            ContentStore store = createMock(ContentStore.class);
            try {
                EasyMock.expect(store.getSpaceAccess(EasyMock.isA(String.class)))
                    .andAnswer(getAccess());
            } catch (ContentStoreException e) {
                // do nothing
            }
            EasyMock.replay(store);
            return store;
        }

        /**
         * This method returns 'open' or 'closed' base no the argument passed
         * to the getContentStore() call above.
         *
         * @return ContentStore.AccessType
         */
        private IAnswer<? extends ContentStore.AccessType> getAccess() {
            return new IAnswer<ContentStore.AccessType>() {
                public ContentStore.AccessType answer() throws Throwable {
                    Object[] args = EasyMock.getCurrentArguments();
                    Assert.assertNotNull(args);
                    Assert.assertEquals(1, args.length);
                    String arg = (String) args[0];
                    if (arg.equals(OPEN_SPACE_ID)) {
                        return ContentStore.AccessType.OPEN;
                    }
                    return ContentStore.AccessType.CLOSED;
                }
            };
        }
    }

}
