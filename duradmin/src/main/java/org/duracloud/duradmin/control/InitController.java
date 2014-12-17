/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import static javax.servlet.http.HttpServletResponse.*;
import static org.duracloud.appconfig.xml.DuradminInitDocumentBinding.*;
import static org.duracloud.common.util.ExceptionUtil.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.common.util.InitUtil;
import org.duracloud.duradmin.config.DuradminConfig;
import org.duracloud.duradmin.domain.AdminInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * This class initializes the application based on the xml body of the
 * servlet request.
 *
 * @author: Andrew Woods
 * Date: Apr 30, 2010
 */
@Controller
@RequestMapping("/init")
public class InitController {

    private static final String APP_NAME = "DurAdmin";

    private final Logger log = LoggerFactory.getLogger(InitController.class);

    private StorageSummaryCache storageSummaryCache; 
    
    private ControllerSupport controllerSupport;
    
    @Autowired
    public InitController(
        ControllerSupport controllerSupport,
        StorageSummaryCache storageSummaryCache) {
        this.storageSummaryCache = storageSummaryCache;
        this.controllerSupport = controllerSupport;
    }
    
    @RequestMapping(method=RequestMethod.POST)
    public ModelAndView initialize(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        log.debug("Initializing DurAdmin");

        ServletInputStream xml = request.getInputStream();
        if (xml != null) {
            try {
                updateInit(createDuradminConfigFrom(xml));
                return respond(response, "Initialization Successful\n", SC_OK);

            } catch (Exception e) {
                return respond(response,
                        getStackTraceAsString(e),
                        SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            return respond(response, "no duradminConfig in request\n", SC_BAD_REQUEST);
        }
    }

    private void updateInit(org.duracloud.appconfig.domain.DuradminConfig config)
        throws Exception {
        AdminInit init = new AdminInit();
        init.setDuraStoreHost(config.getDurastoreHost());
        init.setDuraStorePort(config.getDurastorePort());
        init.setDuraStoreContext(config.getDurastoreContext());
        init.setAmaUrl(config.getAmaUrl());
        init.setDuraBossContext(config.getDurabossContext());
        init.setMillDbEnabled(config.isMillDbEnabled());
        DuradminConfig.setConfig(init);
        

        this.controllerSupport.getContentStoreManager().reinitialize(DuradminConfig.getDuraStoreHost(), 
                                              DuradminConfig.getDuraStorePort(), 
                                              DuradminConfig.getDuraStoreContext());
        
        this.storageSummaryCache.init();

        
    }
    
    @RequestMapping(value="",method=RequestMethod.GET)
    public ModelAndView isInitialized(HttpServletResponse response) {
        if(DuradminConfig.isInitialized()) {
            String text = InitUtil.getInitializedText(APP_NAME);
            return respond(response, text, SC_OK);
        } else {
            String text = InitUtil.getNotInitializedText(APP_NAME);
            return respond(response, text, SC_SERVICE_UNAVAILABLE);
        }
    }

    private ModelAndView respond(HttpServletResponse response, String msg, int status) {
        response.setStatus(status);
        log.info("writing response: status = " + status + "; msg = " + msg);
        return new ModelAndView("jsonView", "response", msg);
    }

    public void setStorageSummaryCache(StorageSummaryCache storageSummaryCache){
        this.storageSummaryCache = storageSummaryCache;
    }
}
