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
import org.duracloud.duradmin.util.MetadataUtils;
import org.duracloud.duradmin.util.NameValuePair;


public class ContentItem
        implements Serializable {

    private static final long serialVersionUID = -5835779644282347055L;

	private String action;

    private String spaceId;

    private String contentId;

	private String storeId;

    private String contentMimetype;

    private String durastoreURL;

    private List<NameValuePair> extendedMetadata;

    private ContentMetadata metadata;

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

    public ContentMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ContentMetadata metadata) {
        this.metadata = metadata;
    }

    public List<NameValuePair> getExtendedMetadata() {
        return extendedMetadata;
    }

    public void setExtendedMetadata(Map<String, String> extendedMetadata) {
        this.extendedMetadata =
                MetadataUtils.convertExtendedMetadata(extendedMetadata);
    }

    
    public void setDurastoreURL(String durastoreURL) {
		this.durastoreURL = durastoreURL;
	}

	public String getDurastoreURL() {
		return durastoreURL;
	}

	public String toString(){
    	return "{storeId: " + storeId + ", spaceId: " + spaceId + ", contentId: " + contentId + 
    				", metadata: " + metadata + ", contentMimetype: " + contentMimetype +"}";
    }
}
