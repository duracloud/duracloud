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
}
