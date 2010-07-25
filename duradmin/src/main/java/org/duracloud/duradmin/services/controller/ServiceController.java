/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.services.controller;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.client.ServicesManager;
import org.duracloud.client.error.NotFoundException;
import org.duracloud.client.error.ServicesException;
import org.duracloud.duradmin.util.ServiceInfoUtil;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.DeploymentOption.Location;
import org.duracloud.serviceconfig.user.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ServiceController implements Controller {

    protected final Logger log = LoggerFactory.getLogger(getClass());
	
    private ServicesManager servicesManager;
    
	public ServicesManager getServicesManager() {
		return servicesManager;
	}

	public void setServicesManager(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Integer serviceId = Integer.valueOf(request.getParameter("serviceId"));

		String method = request.getParameter("method");
		if(method == null){
			method = "";
		}
		
		method = method.toLowerCase();
		if(method == ""){
			return new ModelAndView("jsonView", "serviceInfo", getServiceInfo(serviceId, servicesManager.getDeployedServices()));
		}else{
			if("deploy".equals(method.toLowerCase())){
				return deploy(serviceId, request,response);
			}else{
				Integer deploymentId = Integer.valueOf(request.getParameter("deploymentId"));
				if("undeploy".equals(method)){
					return undeploy(serviceId, deploymentId, request,response);
				}else if("reconfigure".equals(method)){
					return reconfigure(serviceId, deploymentId, request,response);
				}else if("getproperties".equals(method)){
                    return getProperties(serviceId, deploymentId, request,response);
                }
				
				else{
					throw new IllegalArgumentException("method [" + method + "] not recognized");
				}
			}
		}
	}

    private ModelAndView getProperties(Integer serviceId,
                                       Integer deploymentId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws Exception {
        Map<String,String> props = getServicesManager().getDeployedServiceProps(serviceId,deploymentId);
        List<Map<String,String>> propList = new LinkedList<Map<String,String>>();
        for(String key : props.keySet()){
            Map<String,String> map = new LinkedHashMap<String,String>();
            map.put("name", key);
            map.put("value", props.get(key));
            propList.add(map);
        }
        ModelAndView mav = new ModelAndView();
        mav.setViewName("jsonView");
        mav.getModel().clear();
        mav.addObject("properties", propList);
        return mav;
    }

    private ModelAndView reconfigure(Integer serviceId, Integer deploymentId,
			HttpServletRequest request, HttpServletResponse response)  throws ServicesException, NotFoundException{

        List<ServiceInfo> services = this.servicesManager.getDeployedServices();
        ServiceInfo serviceInfo = getServiceInfo(serviceId, services);
        Deployment deployment = getDeployment(serviceInfo, deploymentId);
        List<UserConfig> userConfigs = deployment.getUserConfigs();
        
        if(userConfigs!=null){
            ServiceInfoUtil.applyValues(userConfigs, request);
        }

        String version = serviceInfo.getUserConfigVersion();

        log.info( MessageFormat
                  .format("about to redeploy serviceInfo [id={0}, userConfigVersion={1}, deployment[{2}] ",
                          serviceInfo.getId(),
                          version,
                          deploymentId));

        this.servicesManager.updateServiceConfig(serviceId, deploymentId, version, userConfigs);
        log.info(MessageFormat
                 .format("redeployed service [id={0}, userConfigVersion={1}, deployment[{2}] -  id returned: [{3}] ",
                         serviceInfo.getId(),
                         version,
                         deploymentId));
        
        ServiceInfo newServiceInfo = this.servicesManager.getService(serviceId);
        Deployment newDeployment = getDeployment(newServiceInfo, deploymentId);
        
        ModelAndView mav = new ModelAndView("jsonView", "deployment", newDeployment);
        mav.addObject("serviceInfo", newServiceInfo);
        return mav;
    }

	private Deployment getDeployment(ServiceInfo serviceInfo,
                                     Integer deploymentId) {
	    for(Deployment d : serviceInfo.getDeployments()){
            if(deploymentId == d.getId()){
                return d;
            }
        }
	    
	    return null;
	}

    private ModelAndView undeploy(Integer serviceId, Integer deploymentId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		this.servicesManager.undeployService(serviceId, deploymentId);
		return new ModelAndView("jsonView", "serviceInfo", servicesManager.getService(serviceId));
	}

	



	
	private ModelAndView deploy(Integer serviceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		List<ServiceInfo> services = this.servicesManager.getAvailableServices();
		ServiceInfo serviceInfo = getServiceInfo(serviceId,services);
		List<UserConfig> userConfigs = serviceInfo.getUserConfigs();
		
		if(userConfigs!=null){
			ServiceInfoUtil.applyValues(userConfigs, request);
		}
		String encodedDeployment = request.getParameter("deploymentOption");
		String[] optionId = encodedDeployment.split("-");
        String hostname = optionId[0];
        DeploymentOption.Location location = DeploymentOption.Location.valueOf(optionId[1]);
        DeploymentOption deploymentOption = getDeployment(serviceInfo, hostname, location);

        String version = serviceInfo.getUserConfigVersion();
        String message =
                MessageFormat
                        .format("about to deploy serviceInfo [id={0}, userConfigVersion={1}, deploymentOption[{2}] ",
                                serviceInfo.getId(),
                                version,
                                deploymentOption);

        log.info(message);
        int deploymentId = servicesManager.deployService(serviceInfo.getId(),
                                                         version,
                                                         userConfigs,
                                                         deploymentOption);
        message =
                MessageFormat
                        .format("deployed service [id={0}, userConfigVersion={1}, deploymentOption[{2}] -  id returned: [{3}] ",
                                serviceInfo.getId(),
                                version,
                                deploymentOption,
                                deploymentId);
        log.info(message);
        ServiceInfo newServiceInfo = this.servicesManager.getService(serviceId);
        Deployment deployment = null;
        for(Deployment d : newServiceInfo.getDeployments()){
        	if(deploymentId == d.getId()){
        		deployment = d;
        		break;
        	}
        }
        
        if(deployment == null){
            message =
                MessageFormat
                        .format("unable to find deployment[{0}] in service [id={1}]",
                                deployment,
                        		newServiceInfo.getId());

        	throw new IllegalStateException(message);
        }

        ModelAndView mav = new ModelAndView("jsonView", "deployment", deployment);
        mav.addObject("serviceInfo", newServiceInfo);
        return mav;
		
	}

	private DeploymentOption getDeployment(ServiceInfo serviceInfo,
			String hostname, Location location) {
    	for(DeploymentOption o : serviceInfo.getDeploymentOptions()){
    		if(o.getLocationType().equals(location) && o.getHostname().equals(hostname)){
    			return o;
    		}
    	}
    	
    	throw new RuntimeException("no deployment option found in service [" + 
    									serviceInfo.getId() + "] matching " + location + 
    									" and " + hostname);
	}

	private ServiceInfo getServiceInfo(Integer serviceId, List<ServiceInfo> services) {
		for(ServiceInfo s : services){
			if(s.getId() == serviceId ){
				return s;
			}
		}
    	throw new RuntimeException("no available service where id =  [" + 
				serviceId + "]");
    }


    
}
