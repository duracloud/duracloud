/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.spaces.controller;
     
import org.duracloud.common.util.CountListener;
/**
 * 
 * @author danny
 *
 */
public class ItemCounter implements CountListener{
	private Long count = null;
	
	public Long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	
}
