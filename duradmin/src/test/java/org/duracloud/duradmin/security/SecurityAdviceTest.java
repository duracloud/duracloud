/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.Securable;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author Andrew Woods
 *         Date: Mar 28, 2010
 */
public class SecurityAdviceTest {

    private SecurityAdvice securityAdvice;
    private Securable securable;
    private SecurityContextUtil securityContextUtil;

    private Method method;

    @Before
    public void setUp() throws NoSuchMethodException {
        method = Object.class.getMethod("toString", null);
    }

    @After
    public void tearDown() {
        method = null;
    }

    @Test
    public void testValidUser() throws Throwable {
        boolean valid = true;
        doTest(valid);
    }

    @Test
    public void testInvalidUser() throws Throwable {
        boolean valid = false;
        doTest(valid);
    }

    private void doTest(boolean valid) throws Throwable {
        securable = createMockSecurable(valid);
        securityContextUtil = createMockSecurityContextUtil(valid);

        securityAdvice = new SecurityAdvice(securityContextUtil);
        securityAdvice.before(method, null, securable);
    }

    private Securable createMockSecurable(boolean valid) {
        securable = createMock(Securable.class);
        if (valid) {
            securable.login(isA(Credential.class));
        } else {
            securable.logout();
        }
        expectLastCall();
        replay(securable);

        return securable;
    }

    private SecurityContextUtil createMockSecurityContextUtil(boolean valid)
        throws NoUserLoggedInException {
        securityContextUtil = EasyMock.createMock(SecurityContextUtil.class);
        if (valid) {
            Credential credential = new Credential("un", "pw");
            EasyMock.expect(securityContextUtil.getCurrentUser()).andReturn(
                credential);
        } else {
            EasyMock.expect(securityContextUtil.getCurrentUser())
                .andThrow(new NoUserLoggedInException());
        }

        EasyMock.replay(securityContextUtil);
        return securityContextUtil;
    }

}
