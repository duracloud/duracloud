/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.user;

import java.util.List;


public class MultiSelectUserConfig extends SelectableUserConfig {

    private static final long serialVersionUID = 8515015818197420269L;

    public MultiSelectUserConfig(String name,
                                 String displayName,
                                 List<Option> options) {
        super(name, displayName, options);
    }

    public InputType getInputType() {
        return InputType.MULTISELECT;
    }
    

}
