/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.duracloud.common.model.AclType;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.security.util.AuthorizationHelper;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
/**
 * 
 * @author Daniel Bernstein
 *
 */
@RunWith(EasyMockRunner.class)
public class SnapshotAccessAdviceTest extends EasyMockSupport {

    private String account = "account";
    private String storeId = "store-id";
    private String spaceId = "space-id";
    private String timestamp = "2015-11-13-19-22-26";
    private String snapshotId =
        account + "_" + storeId + "_" + spaceId + "_" + timestamp;
    Map<String, AclType> acls = new HashMap<String, AclType>();

    @Mock
    private AuthorizationHelper helper;

    @Mock
    SecurityContext context;

    @Mock
    private Authentication auth;

    private SnapshotAccessAdvice advice;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    private String[] getArgs(){
        return getArgs("get-snapshot");
    }
    private String[] getArgs(String taskName){
        return new String[]{taskName, "{\"snapshotId\":\"" + snapshotId + "\"}"};
    }
    

    @Test
    public void testSuccessUserAuthorizedSnapshotHistory() throws Throwable {
        testSuccessUserAuthorized("get-snapshot-history");
    }

    @Test
    public void testSuccessUserAuthorizedSnaphsotContents() throws Throwable {
        testSuccessUserAuthorized("get-snapshot-contents");
    }

    @Test
    public void testSuccessNoMethodMatch() throws Throwable {
        replayAll();

        advice = new SnapshotAccessAdvice(helper);

        advice.before(null, getArgs("get-snapshots"), null);
        
    }

    @Test
    public void testSuccessAdmin() throws Throwable {
        expect(helper.hasAdmin(isA(Authentication.class))).andReturn(true);
        replayAll();
        advice = new SnapshotAccessAdvice(helper);
        advice.before(null, getArgs("get-snapshot"), null);
        
    }

    
    public void testSuccessUserAuthorized(String taskName) throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(true);
        replayAll();
        advice.before(null, getArgs(taskName), null);
    }

    @Test
    public void testSuccessNoUserYesGroupPermissions() throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(false);
        expect(helper.groupsHaveReadAccess(eq(auth), eq(acls))).andReturn(true);

        replayAll();
        advice.before(null, getArgs(), null);
    }

    @Test
    public void testFailureNoUserNoGroupPermissions() throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(false);
        expect(helper.groupsHaveReadAccess(eq(auth), eq(acls))).andReturn(false);

        replayAll();
        try {
            advice.before(null, getArgs(), null);
        }catch(UnauthorizedException ex){
            assertTrue("expected failure",true);
        }
    }

    private void setupSubject(){
        advice = new SnapshotAccessAdvice(helper);
        expect(helper.getSpaceACLs(storeId, spaceId)).andReturn(acls);
        SecurityContextHolder.setContext(context);
        expect(context.getAuthentication()).andReturn(auth).atLeastOnce();
        expect(auth.getName()).andReturn("user").atLeastOnce();
        expect(helper.hasAdmin(auth)).andReturn(false);
    }

}
