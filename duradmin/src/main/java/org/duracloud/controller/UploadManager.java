/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */

package org.duracloud.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel Bernstein
 */

public class UploadManager {

	private Logger log = LoggerFactory.getLogger(UploadManager.class);
	private Map<String,UploadTask> uploadTaskMap;

	public UploadManager(){
		this.uploadTaskMap = Collections.synchronizedMap(new HashMap<String,UploadTask>());
	}
	
	public UploadTask addUploadTask(UploadTask uploadTask) throws UploadTaskExistsException{
		if(this.uploadTaskMap.containsKey(uploadTask.getId())){
			UploadTask t = this.uploadTaskMap.get(uploadTask.getId());
			if(t.getState().equals(UploadTask.State.RUNNING)){
				throw new UploadTaskExistsException();
			}
		}
		UploadTask t =  this.uploadTaskMap.put(uploadTask.getId(), uploadTask);
		log.info("upload task added {}", uploadTask.getId());
		return t;
	}
	
	public UploadTask get(String id){
		return this.uploadTaskMap.get(id);
	}
	
	public UploadTask remove(String id){
		log.debug("removing {}", id);
		UploadTask task =  this.uploadTaskMap.remove(id);
		if(task == null){
			log.warn("upload task {} does not exist.", id);
		}else{
			log.info("removed {}", task);
		}
		
		return task;
	}
	
	public List<UploadTask> getUploadTaskList(){
	    return getUploadTaskList(null);
	}

    @SuppressWarnings("unchecked")
    public List<UploadTask> getUploadTaskList(String username) {
        List<UploadTask> list = new LinkedList<UploadTask>();
        list.addAll(this.uploadTaskMap.values());
        if(username != null){
            for(UploadTask t : this.uploadTaskMap.values()){
                if(!username.equals(t.getUsername())){
                    list.remove(t);
                }
            }
        }

        Collections.sort(list);
        return list;
    }
	

}
