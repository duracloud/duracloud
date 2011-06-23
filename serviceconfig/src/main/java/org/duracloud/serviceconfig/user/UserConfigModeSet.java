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
import java.util.Arrays;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 23, 2010
 */
public class UserConfigModeSet implements Serializable, Cloneable {

    public static final String DEFAULT_MODE_NAME = "defaultMode";

	private static final long serialVersionUID = 1L;
	private int id = -1;
    private String name;
    private String displayName;
    private String value;
    private List<UserConfigMode> modes;

    public UserConfigModeSet() {
    }

    public UserConfigModeSet(List<UserConfig> userConfigs) {
        UserConfigMode mode = new UserConfigMode();
        mode.setSelected(true);
        mode.setName(DEFAULT_MODE_NAME);
        mode.setDisplayName("Default Mode");
        mode.setUserConfigs(userConfigs);
        this.displayName = "Default Mode Set";
        this.name = "defaultModeSet";
        this.value = mode.getName();
        this.modes = Arrays.asList(new UserConfigMode[]{mode});
    }

    public boolean hasOnlyUserConfigs() {
        if (null != modes) {
            if (modes.size() == 1) {
                UserConfigMode mode = modes.get(0);
                if (null == mode.getUserConfigModeSets()) {
                    return (null != mode.getUserConfigs() &&
                        mode.getUserConfigs().size() > 0);
                }
            }
        }
        return false;
    }

    public List<UserConfig> wrappedUserConfigs() {
        if (!hasOnlyUserConfigs()) {
            throw new RuntimeException("Not a UserConfigs wrapper object.");
        }
        return modes.get(0).getUserConfigs();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<UserConfigMode> getModes() {
        return modes;
    }

    public void setModes(List<UserConfigMode> modes) {
        this.modes = modes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserConfigModeSet)) {
            return false;
        }

        UserConfigModeSet that = (UserConfigModeSet) o;

        if (id != that.id) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) :
            that.displayName != null) {
            return false;
        }
        if (modes != null ? !modes.equals(that.modes) : that.modes != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result =
            31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (modes != null ? modes.hashCode() : 0);
        return result;
    }

    public UserConfigModeSet clone() throws CloneNotSupportedException {
        UserConfigModeSet clone = (UserConfigModeSet) super.clone();

        clone.setDisplayName(this.displayName);
        clone.setName(this.name);
        clone.setValue(this.value);

        clone.setId(this.id);

        clone.setModes(cloneUserConfigModes());

        return clone;
    }

    private List<UserConfigMode> cloneUserConfigModes()
        throws CloneNotSupportedException {
        List<UserConfigMode> clones = null;
        if (null != this.modes) {
            clones = new ArrayList<UserConfigMode>();
            for (UserConfigMode userConfigMode : this.modes) {
                clones.add(userConfigMode.clone());
            }
        }
        return clones;
    }

}
