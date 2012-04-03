/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.services.controller;

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
public class StreamServiceControllerTest {
    private StreamServiceController controller;
    @Before
    public void setup(){
        controller = new StreamServiceController();
    }

    @After
    public void tearDown(){
        
    }
    
    @Test
    public void testPost() throws Exception{
       boolean enable = true;
       ModelAndView mav = controller.post("testStore", "testSpace", enable);
       Assert.assertEquals(enable, mav.getModelMap().get(StreamServiceController.STREAM_ENABLED_KEY));
    }
    
    @Test
    public void testGet() throws Exception{
        boolean enable = true;
        ModelAndView mav = controller.get("testStore", "testSpace");
        Assert.assertEquals(enable, mav.getModelMap().get(StreamServiceController.STREAM_ENABLED_KEY));
        
    }
}
