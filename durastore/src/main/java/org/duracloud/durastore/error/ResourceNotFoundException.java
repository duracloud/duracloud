/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.error;

/**
 * @author: Bill Branan
 * Date: Jan 8, 2010
 */
public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException(String task,
                                     String spaceId,
                                     Exception e) {
        super(task, spaceId, e);
    }

    public ResourceNotFoundException(String task,
                                     String spaceId,
                                     String contentId,
                                     Exception e) {
        super(task, spaceId, contentId, e);
    }
}
