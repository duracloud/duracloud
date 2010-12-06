/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 23, 2010
 */
public class UserConfigMode implements Serializable, Cloneable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserConfigMode)) {
            return false;
        }

        UserConfigMode that = (UserConfigMode) o;

        if (selected != that.selected) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) :
            that.displayName != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (userConfigModeSets !=
            null ? !userConfigModeSets.equals(that.userConfigModeSets) :
            that.userConfigModeSets != null) {
            return false;
        }
        if (userConfigs != null ? !userConfigs.equals(that.userConfigs) :
            that.userConfigs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result =
            31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (selected ? 1 : 0);
        result =
            31 * result + (userConfigs != null ? userConfigs.hashCode() : 0);
        result = 31 * result +
            (userConfigModeSets != null ? userConfigModeSets.hashCode() : 0);
        return result;
    }

    public UserConfigMode clone() throws CloneNotSupportedException {
        UserConfigMode clone = (UserConfigMode)super.clone();

        clone.setDisplayName(this.displayName);
        clone.setName(this.name);

        clone.setSelected(this.selected);

        clone.setUserConfigModeSets(cloneUserConfigModeSets());
        clone.setUserConfigs(cloneUserConfigs());

        return clone;
    }

    private List<UserConfigModeSet> cloneUserConfigModeSets()
        throws CloneNotSupportedException {
        List<UserConfigModeSet> clones = null;
        if (null != this.userConfigModeSets) {
            clones = new ArrayList<UserConfigModeSet>();
            for (UserConfigModeSet userConfigModeSet : this.userConfigModeSets) {
                clones.add(userConfigModeSet.clone());
            }
        }
        return clones;
    }

    private List<UserConfig> cloneUserConfigs()
        throws CloneNotSupportedException {
        List<UserConfig> clones = null;
        if (null != this.userConfigs) {
            clones = new ArrayList<UserConfig>();
            for (UserConfig userConfig : this.userConfigs) {
                clones.add(userConfig.clone());
            }
        }
        return clones;
    }

}
