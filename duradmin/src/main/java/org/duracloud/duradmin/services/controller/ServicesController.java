/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.services.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.duracloud.client.ServicesManager;
import org.duracloud.serviceconfig.ServiceInfo;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ServicesController implements Controller {

    protected final Logger log = LoggerFactory.getLogger(ServicesController.class);
    
	private ServicesManager servicesManager;
    
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

    
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if("json".equals(request.getParameter("f"))){
			String method = request.getParameter("method");	
			List<ServiceInfo> services = null;
			if("available".equals(method)){
				services = servicesManager.getAvailableServices();	
			}else{
				services = servicesManager.getDeployedServices();
			}

	        ModelAndView mav = new ModelAndView("jsonView");
			mav.addObject("services",services);
			return mav;
		}else{
	        ModelAndView mav = new ModelAndView("services-manager");
	        return mav;
		}
	}
}