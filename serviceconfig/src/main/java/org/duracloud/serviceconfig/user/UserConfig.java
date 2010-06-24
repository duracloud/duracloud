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
public abstract class UserConfig implements Serializable {

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

    public UserConfig(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public abstract InputType getInputType();

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
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
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    public abstract String getDisplayValue();
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result =
            31 * result + (displayName != null ? displayName.hashCode() : 0);
        return result;
    }
    
}
