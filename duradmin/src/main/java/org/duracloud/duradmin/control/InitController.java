/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.duracloud.appconfig.xml.DuradminInitDocumentBinding.createDuradminConfigFrom;
import static org.duracloud.common.util.ExceptionUtil.getStackTraceAsString;

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
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RequestMapping;
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
public class InitController extends BaseCommandController {

    private final Logger log = LoggerFactory.getLogger(InitController.class);

    @Autowired
    private StorageSummaryCache storageSummaryCache; 
    
    public InitController() {
    	setCommandClass(AdminInit.class);
    	setCommandName("adminInit");
    }
    
    protected ModelAndView handle(HttpServletRequest request,
                                  HttpServletResponse response,
                                  Object o,
                                  BindException be) throws Exception {
        String method = request.getMethod();
        if (method.equalsIgnoreCase("POST")) {
            return initialize(request, response);
        } else if(method.equalsIgnoreCase("GET")) {
            return isInitialized(response);
        } else {
            return respond(response, "unsupported: " + method, SC_METHOD_NOT_ALLOWED);
        }
    }

    private ModelAndView initialize(HttpServletRequest request,
                                    HttpServletResponse response) throws Exception {
        log.debug("Initializing " + APP_NAME);

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
        init.setDuraServiceHost(config.getDuraserviceHost());
        init.setDuraServicePort(config.getDuraservicePort());
        init.setDuraServiceContext(config.getDuraserviceContext());
        init.setDuraStoreHost(config.getDurastoreHost());
        init.setDuraStorePort(config.getDurastorePort());
        init.setDuraStoreContext(config.getDurastoreContext());
        init.setAmaUrl(config.getAmaUrl());
        init.setDuraBossContext(config.getDurabossContext());

        DuradminConfig.setConfig(init);

        this.controllerSupport.getContentStoreManager().reinitialize(DuradminConfig.getDuraStoreHost(), 
                                              DuradminConfig.getDuraStorePort(), 
                                              DuradminConfig.getDuraStoreContext());
        
        this.storageSummaryCache.init();

        
    }

    private ModelAndView isInitialized(HttpServletResponse response) {
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
