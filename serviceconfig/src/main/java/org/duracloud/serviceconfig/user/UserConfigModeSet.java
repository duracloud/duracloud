/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 23, 2010
 */
public class UserConfigModeSet {

    private int id = -1;
    private String name;
    private String displayName;
    private String value;
    private List<UserConfigMode> modes;

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
}
