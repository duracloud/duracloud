/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.io.Serializable;

/**
 * * This class holds service config info supplied by the user.
 *
 * @author Andrew Woods
 *         Date: Nov 6, 2009
 */
public abstract class UserConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = -6727102713612538135L;

    /**
     * Directs UI input type.
     */
    public enum InputType {
        SINGLESELECT, MULTISELECT, TEXT;

        public String getName() {
            return name();
        }
    }

    private int id;
    private String name;
    private String displayName;
    private String exclusion = "";

    public UserConfig(String name, String displayName) {
        this(name, displayName, "");
    }

    public UserConfig(String name, String displayName, String exclusion) {
        this.name = name;
        this.displayName = displayName;
        this.exclusion = exclusion;
    }

    public abstract InputType getInputType();

    public int getId() {
        return id;
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

    public String getExclusion() {
        return exclusion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserConfig)) {
            return false;
        }

        UserConfig that = (UserConfig) o;

        if (id != that.id) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) :
            that.displayName != null) {
            return false;
        }
        if (exclusion != null ? !exclusion.equals(that.exclusion) :
            that.exclusion != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
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
        result = 31 * result + (exclusion != null ? exclusion.hashCode() : 0);
        return result;
    }

    public abstract String getDisplayValue();

    public UserConfig clone() throws CloneNotSupportedException {
        UserConfig clone = (UserConfig) super.clone();

        clone.id = this.id;
        clone.displayName = this.displayName;
        clone.exclusion = this.exclusion;
        clone.name = this.name;

        return clone;
    }
    
}
