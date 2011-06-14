/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


/**
 * 
 * @author Daniel Bernstein
 *
 */
@Controller
public class DashboardController {

    protected final Logger log = LoggerFactory.getLogger(DashboardController.class);
    @RequestMapping("/dashboard")
	public ModelAndView get() throws Exception {
        ModelAndView mav = new ModelAndView("dashboard-manager");
        return mav;
	}

 
}