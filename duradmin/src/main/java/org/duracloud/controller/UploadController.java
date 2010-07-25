/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.duradmin.spaces.controller.ContentItemUploadTask;
import org.duracloud.duradmin.spaces.controller.ContentUploadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 *
 * @author Daniel Bernstein
 */

public class UploadController implements Controller{
	private Logger log = LoggerFactory.getLogger(UploadController.class);
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		UploadManager manager = ContentUploadHelper.getManager(request);
		String action = request.getParameter("action");
		String taskId = request.getParameter("taskId");
		if("cancel".equals(action)){
			log.debug("cancelling {}", taskId);
			manager.get(taskId).cancel();
            log.debug("cancelled", taskId);
		}else if("remove".equals(action)){
			log.debug("removing {}" + taskId);
			manager.remove(taskId);
		}

		if(taskId != null){
			return new ModelAndView("jsonView", "task", manager.get(taskId));
		}else{
			return new ModelAndView("jsonView", "taskList", manager.getUploadTaskList(request.getUserPrincipal().getName()));
			
		}
	}
}
