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
import org.duracloud.reportdata.bitintegrity.BitIntegrityReportProperties;
import org.hibernate.validator.constraints.NotBlank;

public class Space implements Serializable {

    private static final long serialVersionUID = 3008516494814826947L;

    @NotBlank
    private String storeId;

    /*
     * The caller's acl based on the granted authorities and group membership of the
     * caller as well as the space acls.
     */
    private String callerAcl;

    private List<Acl> acls;

    private boolean hlsEnabled = false;

    private String action;

    @NotBlank
    private String spaceId;

    private String access;

    private boolean primaryStorageProvider;

    private SpaceProperties properties;

    private List<NameValuePair> extendedProperties;

    private List<String> contents;

    private BitIntegrityReportProperties bitIntegrityReportProperties;

    private boolean millDbEnabled = true;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

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

    private boolean snapshotInProgress = false;

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

    public List<Acl> getAcls() {
        return acls;
    }

    public void setAcls(List<Acl> acls) {
        this.acls = acls;
    }

    /**
     * Indicates whether or not HTTP Live Streaming is enabled.
     * @return boolean value
     */
    public boolean isHlsEnabled() {
        return hlsEnabled;
    }

    /**
     * sets  HTTP Live Streaming enabled flag
     */
    public void setHlsEnabled(boolean hlsEnabled) {
        this.hlsEnabled = hlsEnabled;
    }

    public boolean isPrimaryStorageProvider() {
        return primaryStorageProvider;
    }

    public void setPrimaryStorageProvider(boolean primaryStorageProvider) {
        this.primaryStorageProvider = primaryStorageProvider;
    }

    public void setSnapshotInProgress(boolean snapshotInProgress) {
        this.snapshotInProgress = snapshotInProgress;
    }

    public boolean isSnapshotInProgress() {
        return snapshotInProgress;
    }

    public void setBitIntegrityReportProperties(BitIntegrityReportProperties bitIntegrityReportProperties) {
        this.bitIntegrityReportProperties = bitIntegrityReportProperties;
    }

    public BitIntegrityReportProperties getBitIntegrityReportProperties() {
        return bitIntegrityReportProperties;
    }

    public void setMillDbEnabled(boolean millDbEnabled) {
        this.millDbEnabled = millDbEnabled;
    }

    public boolean isMillDbEnabled() {
        return this.millDbEnabled;
    }
}
