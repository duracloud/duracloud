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

    public List<UserConfigMode> getModes() {
        return modes;
    }

    public void setModes(List<UserConfigMode> modes) {
        this.modes = modes;
    }
}
