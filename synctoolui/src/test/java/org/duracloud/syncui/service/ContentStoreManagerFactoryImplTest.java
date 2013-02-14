/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.duracloud.error.ContentStoreException;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.duracloud.syncui.AbstractTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class ContentStoreManagerFactoryImplTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        super.setup();
    }

    @Test
    public void testCreate() throws ContentStoreException {

        SyncConfigurationManager syncConfigurationManager =
            createMock(SyncConfigurationManager.class);

        DuracloudConfiguration dc = createMock(DuracloudConfiguration.class);

        EasyMock.expect(syncConfigurationManager.retrieveDuracloudConfiguration())
                .andReturn(dc);
        EasyMock.expect(dc.getHost()).andReturn("host");
        EasyMock.expect(dc.getPort()).andReturn(8443);

        replay();

        ContentStoreManagerFactoryImpl csf =
            new ContentStoreManagerFactoryImpl(syncConfigurationManager);

        Assert.assertNotNull(csf.create());

    }

}
