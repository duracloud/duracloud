/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.duradmin.spaces.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.duracloud.controller.UploadManager;

/**
 *
 * @author Daniel Bernstein
 */

public class ContentUploadHelper {

	public static UploadManager getManager(HttpServletRequest request){
		String key = UploadManager.class.getName();
		ServletContext c = request.getSession().getServletContext();
		
		UploadManager u = (UploadManager)c.getAttribute(key);
		
		if(u == null){
			u = new UploadManager();
			c.setAttribute(key, u);
		}
		return u;
	}

	
}
