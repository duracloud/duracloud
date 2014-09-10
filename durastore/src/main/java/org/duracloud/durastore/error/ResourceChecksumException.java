/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.error;

/**
 * @author Bill Branan
 *         Date: 9/4/14
 */
public class ResourceChecksumException extends ResourceException {

    public ResourceChecksumException(String task,
                                     String spaceId,
                                     String contentId,
                                     Exception e) {
        super(task, spaceId, contentId, e);
    }

}
