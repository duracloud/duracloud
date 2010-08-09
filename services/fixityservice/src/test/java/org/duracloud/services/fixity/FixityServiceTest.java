/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class FixityServiceTest {

    private FixityService fixity;
    private File workDir = new File("target/test-fixity-service");

    @Before
    public void setUp() throws Exception {
        fixity = new FixityService();

        if (!workDir.exists()) {
            Assert.assertTrue(workDir.getCanonicalPath(), workDir.mkdir());
        }
        setServiceOptions();
    }

    private void setServiceOptions() throws Exception {
        fixity.setMode(Mode.ALL_IN_ONE_LIST.getKey());
        fixity.setHashApproach(HashApproach.GENERATED.toString());
        fixity.setSalt("abc123");
        fixity.setFailFast(Boolean.TRUE.toString());
        fixity.setStoreId("1");
        fixity.setProvidedListingSpaceIdA("some-space-id");
        fixity.setProvidedListingContentIdA("some-content-id");
        fixity.setOutputSpaceId("output-space-id");
        fixity.setOutputContentId("output-content-id");
        fixity.setReportContentId("report-id");

        fixity.setThreads(3);
        fixity.setServiceWorkDir(workDir.getCanonicalPath());
        fixity.setContentStore(createMockContentStore());

    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore store = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(store.getContent(EasyMock.<String>anyObject(),
                                         EasyMock.<String>anyObject()))
            .andReturn(null);

        EasyMock.replay(store);
        return store;
    }

    @Test
    public void testStart() throws Exception {
        fixity.start();

    }

    @Test
    public void testStop() throws Exception {
    }

    @Test
    public void testGetServiceProps() throws Exception {
    }

}
