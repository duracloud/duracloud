/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.notification.NotificationManager;
import org.duracloud.execdata.ExecConstants;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.serviceapi.ServicesManager;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;

/**
 * @author: Bill Branan
 * Date: 3/15/12
 */
public class HandlerTestBase implements ExecConstants {

    protected ContentStoreManager storeMgr;
    protected ServicesManager servicesMgr;
    protected ManifestGenerator manifestGenerator;
    protected ContentStore contentStore;
    protected NotificationManager notifier;

    @Before
    public void setup() throws Exception {
        storeMgr = EasyMock
            .createMock("ContentStoreManager", ContentStoreManager.class);
        servicesMgr = EasyMock.createMock("ServicesManager",
                                           ServicesManager.class);
        manifestGenerator = EasyMock.createMock("ManifestGenerator",
                                                ManifestGenerator.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        notifier = EasyMock.createMock("NotificationManager",
                                       NotificationManager.class);

        EasyMock.expect(storeMgr.getPrimaryContentStore())
                .andReturn(contentStore)
                .anyTimes();

        EasyMock.expect(contentStore.getSpaceProperties(
            BaseServiceHandler.HANDLER_STATE_SPACE))
                .andReturn(null)
                .anyTimes();
    }

    protected void replayMocks() {
        EasyMock.replay(storeMgr, servicesMgr, manifestGenerator,
                        contentStore, notifier);
    }

    @After
    public void teardown() {
        EasyMock.verify(storeMgr, servicesMgr, manifestGenerator,
                        contentStore, notifier);
    }

}
