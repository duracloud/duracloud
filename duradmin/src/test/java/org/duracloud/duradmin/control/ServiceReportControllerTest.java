/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.report.ServiceReportManager;
import org.duracloud.client.report.error.NotFoundException;
import org.duracloud.client.report.error.ReportException;
import org.duracloud.common.model.Credential;
import org.duracloud.domain.Content;
import org.duracloud.duradmin.test.AbstractTestBase;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class ServiceReportControllerTest extends AbstractTestBase {
    private ServiceReportController controller;

    @Override
    @Before
    public void setup() {
        super.setup();
    }

    @Test
    public void testGetInfo()
        throws ReportException,
            NotFoundException,
            ContentStoreException,
            IOException {
        ServiceReportManager srm = createMock(ServiceReportManager.class);
        srm.login(EasyMock.isA(Credential.class));
        EasyMock.expectLastCall();
        ContentStoreManager csm = createMock(ContentStoreManager.class);

        ContentStore cs = createMock(ContentStore.class);
        Content c = createMock(Content.class);
        EasyMock.expect(cs.getContent(EasyMock.isA(String.class),
                                      EasyMock.isA(String.class))).andReturn(c);
        Map<String, String> map = new HashMap<String, String>();
        String sizeParamName = "size";

        map.put(ContentStore.CONTENT_SIZE, "testSize");
        EasyMock.expect(c.getProperties()).andReturn(map);
        EasyMock.expect(csm.getPrimaryContentStore()).andReturn(cs);
        replay();
        controller = new ServiceReportController(srm, csm);
        ModelAndView mav = controller.getInfo(null, "testSpace", "testContent");
        Assert.assertNotNull(((Map<String, String>) mav.getModel()
                                                       .get("fileInfo")).get(sizeParamName));
    }

}
