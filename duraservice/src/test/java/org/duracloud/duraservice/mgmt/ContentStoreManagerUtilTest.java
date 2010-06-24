/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.duraservice.domain.Store;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Apr 1, 2010
 */
public class ContentStoreManagerUtilTest {

    private Credential userCredential = new Credential("user", "pword");
    private Store store;

    @Before
    public void setUp() {
        store = new Store();
        store.setHost("test.host");
        store.setPort("9999");
        store.setContext("test.context");
    }

    @After
    public void tearDown() {
        store = null;
    }

    @Test
    public void testGetContentStoreManager() throws NoUserLoggedInException {
        boolean loggedIn = true;
        doTestGetContentStoreManager(loggedIn);

        loggedIn = false;
        doTestGetContentStoreManager(loggedIn);
    }

    private void doTestGetContentStoreManager(boolean loggedIn)
        throws NoUserLoggedInException {
        SecurityContextUtil securityCxt = createMockSecurityContextUtil(loggedIn);
        ContentStoreManagerUtil util = new ContentStoreManagerUtil(securityCxt);

        ContentStoreManager storeMgr = util.getContentStoreManager(store);
        Assert.assertNotNull(storeMgr);
    }

    @Test
    public void testGetCurrentUser() throws NoUserLoggedInException {
        boolean loggedIn = true;
        doTestGetCurrentUser(loggedIn);

        loggedIn = false;
        doTestGetCurrentUser(loggedIn);
    }

    private void doTestGetCurrentUser(boolean loggedIn)
        throws NoUserLoggedInException {
        SecurityContextUtil securityCxt = createMockSecurityContextUtil(loggedIn);
        ContentStoreManagerUtil util = new ContentStoreManagerUtil(securityCxt);

        Credential credential = util.getCurrentUser();
        Assert.assertNotNull(credential);

        if (loggedIn) {
            Assert.assertEquals(userCredential.getUsername(),
                                credential.getUsername());
            Assert.assertEquals(userCredential.getPassword(),
                                credential.getPassword());
        } else {
            Assert.assertEquals("unknown", credential.getUsername());
            Assert.assertEquals("unknown", credential.getPassword());
        }
    }

    private SecurityContextUtil createMockSecurityContextUtil(boolean loggedIn)
        throws NoUserLoggedInException {
        SecurityContextUtil securityCxt = EasyMock.createMock(
            SecurityContextUtil.class);
        if (loggedIn) {
            EasyMock.expect(securityCxt.getCurrentUser()).andReturn(
                userCredential);
        } else {
            EasyMock.expect(securityCxt.getCurrentUser())
                .andThrow(new NoUserLoggedInException());
        }
        EasyMock.replay(securityCxt);
        return securityCxt;
    }
}
