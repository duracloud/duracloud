/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.io.Serializable;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 23, 2010
 */
public class UserConfigMode implements Serializable {

	private static final long serialVersionUID = 1L;
	/**
     * These fields describe this config mode.
     */
    private String name;
    private String displayName;
    private boolean selected;

    /**
     * These fields hold nested config options associated with this mode.
     */
    private List<UserConfig> userConfigs;
    private List<UserConfigModeSet> userConfigModeSets;

    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public List<UserConfig> getUserConfigs() {
        return userConfigs;
    }

    public void setUserConfigs(List<UserConfig> userConfigs) {
        this.userConfigs = userConfigs;
    }

    public List<UserConfigModeSet> getUserConfigModeSets() {
        return userConfigModeSets;
    }

    public void setUserConfigModeSets(List<UserConfigModeSet> userConfigModeSets) {
        this.userConfigModeSets = userConfigModeSets;
    }
}
