/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 *  Base class for all dtos.
 *
 * @author Daniel Bernstein
 *         Date: 8/21/14
 */
public class BaseDTO {

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
