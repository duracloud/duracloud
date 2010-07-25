/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.controller;

import java.lang.reflect.ParameterizedType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

/**
 * 
 * @author Daniel Bernstein
 */
public class AbstractRestController<T> extends AbstractCommandController{
	public AbstractRestController(String commandName){
		super();
		Class commandClass = (((Class)((ParameterizedType)this.getClass().
			       getGenericSuperclass()).getActualTypeArguments()[0]));
		setCommandClass(commandClass);
		if(commandName != null && commandName.trim() != ""){
			setCommandName(commandName);
		}
		
	}
	
	

	protected ModelAndView handle(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {
		String method = request.getMethod().toUpperCase();
		String action = request.getParameter("action");
		if(action == null){
			action = "";
		}
		
		action = action.toLowerCase();

		if(method.equals("GET")){
			return get(request, response, (T)command, errors);
		}else if(method.equals("POST")){
			if("delete".equals(action)){
				return delete(request, response, (T)command, errors);
			}else if("put".equals(action)){
				return put(request, response, (T)command, errors);
			}else{
				return post(request, response, (T)command, errors);
			}
		}else if(method.equals("PUT")){
			return put(request, response, (T)command, errors);
		}else if(method.equals("DELETE")){
			return delete(request, response, (T)command, errors);
		}else{
			throw new IllegalArgumentException("method not supported: " + method);
		}
	}

	protected ModelAndView get(HttpServletRequest request,
			HttpServletResponse response, T command, BindException errors) throws Exception{
		throw new NotImplementedException();
	}
	
	protected ModelAndView put(HttpServletRequest request,
			HttpServletResponse response, T command, BindException errors)  throws Exception{
		throw new NotImplementedException();
	}

	protected ModelAndView post(HttpServletRequest request,
			HttpServletResponse response, T command, BindException errors)  throws Exception{
		throw new NotImplementedException();
	}

	protected ModelAndView delete(HttpServletRequest request,
			HttpServletResponse response, T command, BindException errors)  throws Exception{
		throw new NotImplementedException();
	}

	
}
