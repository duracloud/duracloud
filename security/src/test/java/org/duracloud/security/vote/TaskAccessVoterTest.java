/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.duracloud.security.domain.HttpVerb;
import org.duracloud.security.impl.DuracloudUserDetails;
import org.duracloud.snapshot.SnapshotConstants;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.FilterInvocation;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@RunWith(EasyMockRunner.class)
public class TaskAccessVoterTest extends EasyMockSupport {

    @TestSubject
    private TaskAccessVoter voter;
    
    @Mock
    private HttpServletRequest request;
    @Mock
    private FilterInvocation resource;

    
    @Before
    public void setUp() throws Exception {
        voter = new TaskAccessVoter();
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    @Test
    public void testAbstainForNonTaskRelatedPaths() {
        TaskAccessVoter voter = new TaskAccessVoter();
        Authentication auth = registeredUser(LOGIN.ADMIN,"");
        FilterInvocation resource = createMockInvocation(HttpVerb.GET, "/nontask");
        replayAll();
        assertEquals(AccessDecisionVoter.ACCESS_ABSTAIN, voter.vote(auth, resource, null));
    }

    @Test
    public void testNonRootRestoreCallDenied() {
        TaskAccessVoter voter = new TaskAccessVoter();
        Authentication auth = registeredUser(LOGIN.ADMIN,"");
        FilterInvocation resource =
            createMockInvocation(HttpVerb.POST,
                                 "/task/" + SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME);
        replayAll();
        assertEquals(AccessDecisionVoter.ACCESS_DENIED, voter.vote(auth, resource, null));
    }

    @Test
    public void testRootRestoreCallGranted() {
        TaskAccessVoter voter = new TaskAccessVoter();
        Authentication auth = registeredUser(LOGIN.ROOT,"");
        FilterInvocation resource =
            createMockInvocation(HttpVerb.POST,
                                 "/task/" + SnapshotConstants.RESTORE_SNAPSHOT_TASK_NAME);
        replayAll();
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, voter.vote(auth, resource, null));
    }

    @Test
    public void testAllOtherGranted (){
        TaskAccessVoter voter = new TaskAccessVoter();
        Authentication auth = registeredUser(LOGIN.ADMIN,"");
        FilterInvocation resource =
            createMockInvocation(HttpVerb.POST,
                                 "/task/not-restore-snapshot");
        replayAll();
        assertEquals(AccessDecisionVoter.ACCESS_GRANTED, voter.vote(auth, resource, null));
    }

    
    private FilterInvocation createMockInvocation(HttpVerb method,
                                                  String path) {
        
        
        EasyMock.expect(request.getPathInfo()).andReturn(path).times(1);
        EasyMock.expect(request.getMethod()).andReturn(method.name()).times(1);
        EasyMock.expect(resource.getHttpRequest()).andReturn(request);
        return resource;
    }
    
    private Authentication registeredUser(LOGIN login, String group) {
        List<String> groups = new ArrayList<String>();
        groups.add(group);

        DuracloudUserDetails user = new DuracloudUserDetails(login.name(),
                                                             "x",
                                                             "email",
                                                             "",
                                                             true,
                                                             true,
                                                             true,
                                                             true,
                                                             login.auths,
                                                             groups);
        return new UsernamePasswordAuthenticationToken(user, "", login.auths);
    }
    
    protected Collection<ConfigAttribute> createConfig(SecurityConfig role) {
        List<ConfigAttribute> configAttributes = new LinkedList<>();
        configAttributes.add(role);
        return configAttributes;
    }

    
    private enum LOGIN {
        ROOT("ROLE_ROOT"),
        ADMIN("ROLE_ADMIN");

        private Collection<GrantedAuthority> auths;

        LOGIN(String role) {
            auths = Arrays.asList(new GrantedAuthority[]{new SimpleGrantedAuthority(role)});
        }
    }
}
