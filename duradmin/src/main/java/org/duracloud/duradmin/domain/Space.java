/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.duracloud.duradmin.util.MetadataUtils;
import org.duracloud.duradmin.util.NameValuePair;

public class Space
        implements Serializable {

    private static final long serialVersionUID = 3008516494814826947L;

	private String storeId;

    public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

    
    private String action;

    private String spaceId;

    private String access;

    private SpaceMetadata metadata;

    private List<NameValuePair> extendedMetadata;

    private List<String> contents;

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

    public SpaceMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SpaceMetadata metadata) {
        this.metadata = metadata;
    }

    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
    }

    public List<NameValuePair> getExtendedMetadata() {
        return extendedMetadata;
    }

    public void setExtendedMetadata(Map<String, String> extendedMetadata) {
        this.extendedMetadata =
                MetadataUtils.convertExtendedMetadata(extendedMetadata);
    }

}
