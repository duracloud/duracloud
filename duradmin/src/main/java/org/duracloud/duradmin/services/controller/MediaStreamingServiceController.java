/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.services.controller;

import org.duracloud.client.exec.Executor;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.duradmin.util.ServiceUtil;
import org.duracloud.execdata.mediastreaming.MediaStreamingConstants;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@RequestMapping(value = "/services/mediastreamer")
public class MediaStreamingServiceController {

    private static final String STREAMING_HOST_KEY = "streamingHost";
    protected static final String STREAMING_ENABLED_KEY = "streamingEnabled";
    protected final Logger log =
        LoggerFactory.getLogger(MediaStreamingServiceController.class);

    private Executor executor;
    private ServicesManager servicesManager;

    @Autowired
    public MediaStreamingServiceController(@Qualifier("executor") Executor executor,
                                           @Qualifier("servicesManager") ServicesManager servicesManager){
        this.executor = executor;
        this.executor.login(new RootUserCredential());
        this.servicesManager = servicesManager;
    }
    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView post(@RequestParam(required = true) String storeId,
                             @RequestParam(required = true) String spaceId,
                             @RequestParam(required = true) boolean enable)
        throws Exception {
        try{
            
            String action =
                enable
                    ? MediaStreamingConstants.START_STREAMING
                    : MediaStreamingConstants.STOP_STREAMING;
            this.executor.performAction(action, spaceId);
            log.info("successfully "
                + (enable ? "enabled" : "disabled")
                + " the stream service for space (" + spaceId
                + ") on storage provider (" + storeId + ")");
            ModelAndView mav =
                new ModelAndView("jsonView", STREAMING_ENABLED_KEY, enable);
            return mav;
            
        }catch(Exception ex){
            throw new DuraCloudRuntimeException(ex);
        }
    }
    
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView getState(@RequestParam(required = true) String storeId,
                             @RequestParam(required = true) String spaceId)
        throws Exception {
        boolean enabled;
        String streamingHost = null;
        try{
            
            ServiceInfo service = ServiceUtil.findMediaStreamingService(servicesManager);
            enabled = ServiceUtil.isMediaStreamingServiceEnabled(service, spaceId);
            if(enabled){
                streamingHost = ServiceUtil.getStreamingHost(servicesManager, service, spaceId);
            }
        }catch(NotFoundException ex){
            enabled = false;
        }catch(ServicesException ex){
            throw new DuraCloudRuntimeException(ex);
        }
        
        ModelAndView mav =
            new ModelAndView("jsonView");
        mav.addObject(STREAMING_ENABLED_KEY, enabled);
        mav.addObject(STREAMING_HOST_KEY, streamingHost);

        return mav;
        
    }
}
