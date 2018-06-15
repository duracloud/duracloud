/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.error;

/**
 * An exception that indicates that one or more property names and or values are invalid.
 *
 * @author: Daniel Bernstein
 * Date: Jun 15, 2018
 */
public class ResourcePropertiesInvalidException extends ResourceException {
    public ResourcePropertiesInvalidException(String task,
                                              String spaceId,
                                              String contentId,
                                              Exception ex) {
        super(task, spaceId, contentId, ex);
    }
}