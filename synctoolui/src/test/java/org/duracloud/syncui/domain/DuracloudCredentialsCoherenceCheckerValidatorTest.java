/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.syncui.service.ContentStoreFactory;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class DuracloudCredentialsCoherenceCheckerValidatorTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testIsValid() throws Exception{
        ContentStoreFactory f = EasyMock.createMock(ContentStoreFactory.class);
        DuracloudCredentialsForm credentials = new DuracloudCredentialsForm();
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(f.create(credentials)).andReturn(contentStore);
        EasyMock.replay(f, contentStore);
        DuracloudCredentialsCoherenceCheckerValidator v = new DuracloudCredentialsCoherenceCheckerValidator(f);
        Assert.assertTrue(v.isValid(credentials, null));
        EasyMock.verify(f, contentStore);
    }

    @Test
    public void testIsInvalid() throws Exception{
        ContentStoreFactory f = EasyMock.createMock(ContentStoreFactory.class);
        DuracloudCredentialsForm credentials = new DuracloudCredentialsForm();
        EasyMock.expect(f.create(credentials)).andThrow(new ContentStoreException("test exception"));
        EasyMock.replay(f);
        DuracloudCredentialsCoherenceCheckerValidator v = new DuracloudCredentialsCoherenceCheckerValidator(f);
        Assert.assertFalse(v.isValid(credentials, null));
        EasyMock.verify(f);
    }

}
