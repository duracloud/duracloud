/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.services.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * 
 * @author Daniel Bernstein Date: April 2, 2012
 */
@Controller
@RequestMapping(value = "/services/streamservice")
public class StreamServiceController {

    protected static final String STREAM_ENABLED_KEY = "streamEnabled";
    protected final Logger log =
        LoggerFactory.getLogger(StreamServiceController.class);

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(@RequestParam(required = true) String storeId,
                             @RequestParam(required = true) String spaceId,
                             @RequestParam(required = true) boolean enable)
        throws Exception {

        // TODO make duraboss api call here.
        log.info("successfully "
            + (enable ? "enabled" : "disabled")
            + " the stream service for space (" + spaceId
            + ") on storage provider (" + storeId + ")");
        ModelAndView mav =
            new ModelAndView("jsonView", STREAM_ENABLED_KEY, enable);
        return mav;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(@RequestParam(required = true) String storeId,
                            @RequestParam(required = true) String spaceId)
        throws Exception {
        
        boolean enabled = true;  // TODO make duraboss api call here.
        
        ModelAndView mav =
            new ModelAndView("jsonView", STREAM_ENABLED_KEY, enabled);
        return mav;
    }

}
