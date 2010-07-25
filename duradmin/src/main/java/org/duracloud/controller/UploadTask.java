/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.controller;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Daniel Bernstein
 */


@SuppressWarnings("unchecked")
public interface UploadTask extends Comparable{
	public String getId();
	
	
	public void cancel();

	
	public Map<String,String> getProperties();


	public Date getStartDate();


    public String getUsername();
	
	public static enum State {
		INITIALIZED,
		RUNNING,
		SUCCESS,
		FAILURE,
		CANCELLED
	}
	
	public State getState();
	
}
