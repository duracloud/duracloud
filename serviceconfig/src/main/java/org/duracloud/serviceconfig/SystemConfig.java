/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import java.io.Serializable;

/**
 * This class holds service config info supplied by the user.
 *
 * @author Andrew Woods
 *         Date: Nov 6, 2009
 */
public class SystemConfig implements Serializable {

    private static final long serialVersionUID = -3280385789614105156L;
    private int id;
    private String name;
    private String value;
    private String defaultValue;

    public SystemConfig(String name, String value, String defaultValue) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SystemConfig)) {
            return false;
        }

        SystemConfig that = (SystemConfig) o;

        if (id != that.id) {
            return false;
        }
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) :
            that.defaultValue != null) {
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
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result =
            31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        return result;
    }
}
