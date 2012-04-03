/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.duracloud.duradmin.util.NameValuePair;
import org.duracloud.duradmin.util.PropertiesUtils;

public class Space
        implements Serializable {

    private static final long serialVersionUID = 3008516494814826947L;

	private String storeId;
	
	/*
	 * The caller's acl based on the granted authorities and group membership of the 
	 * caller as well as the space acls. 
	 */
	private String callerAcl;

	private List<Acl> acls; 
	
	private boolean streamingEnabled = false;
	
    public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	
    private String action;

    private String spaceId;

    private String access;

    private SpaceProperties properties;

    private List<NameValuePair> extendedProperties;

    private List<String> contents;

    /**
	 * the count of all the items in the space
	 */
    public Long getItemCount() {
		return itemCount;
	}

	public void setItemCount(Long itemCount) {
		this.itemCount = itemCount;
	}

	private Long itemCount = null;
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public SpaceProperties getProperties() {
        return properties;
    }

    public void setProperties(SpaceProperties properties) {
        this.properties = properties;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public List<NameValuePair> getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(Map<String, String> extendedProperties) {
        this.extendedProperties =
                PropertiesUtils.convertExtendedProperties(extendedProperties);
    }

    public String getCallerAcl() {
        return callerAcl;
    }

    public void setCallerAcl(String callerAcl) {
        this.callerAcl = callerAcl;
    }

    public List<Acl>  getAcls() {
        return acls;
    }

    public void setAcls(List<Acl> acls) {
        this.acls = acls;
    }

    public boolean isStreamingEnabled() {
        return streamingEnabled;
    }

    public void setStreamingEnabled(boolean streamingEnabled) {
        this.streamingEnabled = streamingEnabled;
    }

}
