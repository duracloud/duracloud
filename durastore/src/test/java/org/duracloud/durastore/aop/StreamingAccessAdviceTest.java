/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.duracloud.StorageTaskConstants;
import org.duracloud.common.model.AclType;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.security.util.AuthorizationHelper;
import org.duracloud.storage.provider.TaskProvider;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * @author Daniel Bernstein
 */
@RunWith(EasyMockRunner.class)
public class StreamingAccessAdviceTest extends EasyMockSupport {

    private String storeId = "store-id";
    private String spaceId = "space-id";
    Map<String, AclType> acls = new HashMap<String, AclType>();

    @Mock
    private AuthorizationHelper helper;

    @Mock
    SecurityContext context;

    @Mock
    private Authentication auth;

    @Mock
    private TaskProvider taskProvider;

    private StreamingAccessAdvice advice;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        verifyAll();
    }

    private String[] getArgs() {
        return getArgs(StorageTaskConstants.GET_HLS_URL_TASK_NAME);
    }

    private String[] getArgs(String taskName) {
        GetUrlTaskParameters parameters = new GetUrlTaskParameters();
        parameters.setSpaceId(spaceId);
        parameters.setContentId("content-id");
        return new String[] {taskName, parameters.serialize()};
    }

    @Test
    public void testSuccessUserAuthorizedGetUrl() throws Throwable {
        testSuccessUserAuthorized(StorageTaskConstants.GET_HLS_URL_TASK_NAME);
    }

    @Test
    public void testSuccessUserAuthorizedGetSignedUrl() throws Throwable {
        testSuccessUserAuthorized(StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME);
    }

    @Test
    public void testSuccessNoMethodMatch() throws Throwable {
        replayAll();

        advice = new StreamingAccessAdvice(helper);

        advice.before(null, getArgs("no-match-method"), taskProvider);

    }

    @Test
    public void testSuccessAdmin() throws Throwable {
        expect(helper.hasAdmin(isA(Authentication.class))).andReturn(true);
        setupTaskProvider();
        replayAll();
        advice = new StreamingAccessAdvice(helper);
        advice.before(null, getArgs(StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME), taskProvider);
    }

    protected void setupTaskProvider() {
        expect(this.taskProvider.getStoreId()).andReturn(storeId);
    }

    private void testSuccessUserAuthorized(String taskName) throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(true);
        setupTaskProvider();
        replayAll();
        advice.before(null, getArgs(taskName), taskProvider);
    }

    @Test
    public void testSuccessNoUserYesGroupPermissions() throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(false);
        expect(helper.groupsHaveReadAccess(eq(auth), eq(acls))).andReturn(true);
        setupTaskProvider();
        replayAll();
        invokeAdvice();
    }

    protected void invokeAdvice() throws Throwable {
        advice.before(null, getArgs(StorageTaskConstants.GET_HLS_URL_TASK_NAME), taskProvider);
    }

    protected void invokeAdvice(String taskName) throws Throwable {
        advice.before(null, getArgs(taskName), taskProvider);
    }

    @Test
    public void testFailureNoUserNoGroupPermissions() throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(false);
        expect(helper.groupsHaveReadAccess(eq(auth), eq(acls))).andReturn(false);
        setupTaskProvider();

        replayAll();
        try {
            invokeAdvice();
        } catch (UnauthorizedException ex) {
            assertTrue("expected failure", true);
        }
    }

    @Test
    public void testFailureNoUserNoGroupPermissionsGetSignedUrl() throws Throwable {
        setupSubject();
        expect(helper.hasReadAccess(isA(String.class),
                                    eq(acls))).andReturn(false);
        expect(helper.groupsHaveReadAccess(eq(auth), eq(acls))).andReturn(false);
        setupTaskProvider();

        replayAll();
        try {
            invokeAdvice(StorageTaskConstants.GET_SIGNED_COOKIES_URL_TASK_NAME);
        } catch (UnauthorizedException ex) {
            assertTrue("expected failure", true);
        }
    }

    private void setupSubject() {
        advice = new StreamingAccessAdvice(helper);
        expect(helper.getSpaceACLs(storeId, spaceId)).andReturn(acls);
        SecurityContextHolder.setContext(context);
        expect(context.getAuthentication()).andReturn(auth).atLeastOnce();
        expect(auth.getName()).andReturn("user").atLeastOnce();
        expect(helper.hasAdmin(auth)).andReturn(false);
    }

}
