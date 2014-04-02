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

import org.duracloud.common.web.EncodeUtil;
import org.duracloud.duradmin.util.NameValuePair;
import org.duracloud.duradmin.util.PropertiesUtils;
import org.hibernate.validator.constraints.NotBlank;


public class ContentItem
        implements Serializable {

    private static final long serialVersionUID = -5835779644282347055L;

    
	private String action;

	@NotBlank
    private String spaceId;

    @NotBlank
    private String contentId;

    @NotBlank
	private String storeId;
	
	private boolean primaryStorageProvider;

    private String contentMimetype;

    private String durastoreURL;

    private List<NameValuePair> extendedProperties;

    private ContentProperties properties;

    private String imageViewerBaseURL;
    /*
     * The caller's acl based on the granted authorities and group membership of the 
     * caller as well as the space acls. 
     */
    private String callerAcl;
    
    private List<Acl> acls; 
    
    public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	
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

    public String getContentId() {
        return this.contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    
    public String getEncodedContentId() {
        String contentId = getContentId();
        return EncodeUtil.urlEncode(contentId);
    }

    public String getContentMimetype() {
        return this.contentMimetype;
    }

    public void setContentMimetype(String contentMimetype) {
        this.contentMimetype = contentMimetype;
    }

    public ContentProperties getProperties() {
        return properties;
    }

    public void setProperties(ContentProperties properties) {
        this.properties = properties;
    }

    public List<NameValuePair> getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(Map<String, String> extendedProperties) {
        this.extendedProperties =
                PropertiesUtils.convertExtendedProperties(extendedProperties);
    }

    
    public void setDurastoreURL(String durastoreURL) {
		this.durastoreURL = durastoreURL;
	}

	public String getDurastoreURL() {
		return durastoreURL;
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
    
	public String toString(){
    	return "{storeId: " + storeId + ", spaceId: " + spaceId + ", contentId: " + contentId + 
    				", properties: " + properties + ", contentMimetype: " + contentMimetype +
    				", callerAcl: " + callerAcl + "}";
    }

	public String getImageViewerBaseURL() {
        return imageViewerBaseURL;
    }

    public void setImageViewerBaseURL(String imageViewerBaseURL) {
        this.imageViewerBaseURL = imageViewerBaseURL;
    }

    public boolean isPrimaryStorageProvider() {
        return primaryStorageProvider;
    }

    public void setPrimaryStorageProvider(boolean primaryStorageProvider) {
        this.primaryStorageProvider = primaryStorageProvider;
    }
}
