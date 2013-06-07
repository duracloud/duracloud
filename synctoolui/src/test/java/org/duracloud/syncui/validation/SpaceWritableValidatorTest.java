/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.validation;

import java.io.InputStream;
import java.util.HashMap;

import javax.validation.ConstraintValidatorContext;

import org.duracloud.client.ContentStore;
import org.duracloud.syncui.AbstractTest;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.ContentStoreFactory;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class SpaceWritableValidatorTest  extends AbstractTest {
    
    @Test
    public void testIsValid() throws Exception {
        ContentStore cs = createMock(ContentStore.class);
        ContentStoreFactory csf =  createMock(ContentStoreFactory.class);
        EasyMock.expect(csf.create(EasyMock.anyObject(DuracloudCredentialsForm.class)))
                .andReturn(cs);
        EasyMock.expect(cs.addContent(EasyMock.isA(String.class),
                                      EasyMock.isA(String.class),
                                      EasyMock.isA(InputStream.class),
                                      EasyMock.anyLong(),
                                      EasyMock.isA(String.class),
                                      EasyMock.isNull(String.class),
                                      EasyMock.isNull(new HashMap<String, String>().getClass()))).andReturn("");

        cs.deleteContent(EasyMock.isA(String.class),
                                      EasyMock.isA(String.class));
        
        ConstraintValidatorContext cvc = createMock(ConstraintValidatorContext.class);
        replay();

        SpaceWritableValidator v = new SpaceWritableValidator(csf);


        SpaceForm f = new SpaceForm();
        f.setSpaceId("testSpace");
        Assert.assertTrue(v.isValid(f, cvc));
    }

}
