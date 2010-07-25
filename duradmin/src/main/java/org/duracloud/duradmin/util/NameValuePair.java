/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.io.Serializable;

public class NameValuePair
        implements Serializable {

    private static final long serialVersionUID = 1L;

    public NameValuePair(String name, Object value) {
        super();
        this.name = name;
        Value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return Value;
    }

    private String name;

    private Object Value;
}