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
 * This class holds config option details.
 */
public class Option implements Serializable {

    private static final long serialVersionUID = -2243245528826127669L;

    private String displayName;
    private String value;
    private boolean selected;

    public Option(String displayName, String value, boolean selected) {
        super();
        this.displayName = displayName;
        this.value = value;
        this.selected = selected;
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Option)) {
            return false;
        }

        Option option = (Option) o;

        if (selected != option.selected) {
            return false;
        }
        if (displayName != null ? !displayName.equals(option.displayName) :
            option.displayName != null) {
            return false;
        }
        if (value != null ? !value.equals(option.value) :
            option.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName != null ? displayName.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (selected ? 1 : 0);
        return result;
    }
}
