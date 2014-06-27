/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
/**
 * A spring controller for error list viewing and navigation
 *
 * @author Daniel Bernstein
 *
 */
@Controller
public class ErrorsController {
    private static Logger log = LoggerFactory.getLogger(ErrorsController.class);
    @RequestMapping(value= {"/errors"})
    public String get(){
        log.debug("accessing errors page");
        return "errors";
    }
    
    
    @RequestMapping(value= {"/exception"})
    public String exception(Model model){
        model.addAttribute("message",
                           "Unknown error");
        return "exception";
    }
    
    @RequestMapping(value= {"/404"})
    public String error404(Model model){
        model.addAttribute("message", "404: Page not found.");
        return "exception";
    }

    @RequestMapping(value= {"/test-error"})
    public String testError(Model model){
         throw new RuntimeException("This is only a test!");
    }

}
