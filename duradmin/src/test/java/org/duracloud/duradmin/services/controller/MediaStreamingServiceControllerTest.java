/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.services.controller;

import org.duracloud.duradmin.test.AbstractTestBase;
import org.duracloud.exec.Executor;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.serviceapi.ServicesManager;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class MediaStreamingServiceControllerTest extends AbstractTestBase {
    private MediaStreamingServiceController controller;

    @Override
    @Before
    public void setup(){
        super.setup();
        ServicesManager servicesManager = createMock(ServicesManager.class);

        Executor executor = createMock(Executor.class);
        try {
            executor.performAction(EasyMock.isA(String.class),
                                   EasyMock.isA(String.class));
        } catch (InvalidActionRequestException e) {
            Assert.fail("Unexpected exception: "+e.getMessage());
        }
        EasyMock.expectLastCall();
        replay();
        
        controller = new MediaStreamingServiceController(executor, servicesManager);
    }

    @Override
    @After
    public void tearDown(){
        super.tearDown();
    }
    
    @Test
    public void testPost() throws Exception{
       boolean enable = true;
       ModelAndView mav = controller.post("testStore", "testSpace", enable);
       Assert.assertEquals(enable, mav.getModelMap().get(MediaStreamingServiceController.STREAMING_ENABLED_KEY));
    }
}
