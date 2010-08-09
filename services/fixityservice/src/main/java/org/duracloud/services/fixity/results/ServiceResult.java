/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

/**
 * This interface is for result objects in the ServiceResultProcessing
 * framework.
 *
 * @author Andrew Woods
 *         Date: Aug 4, 2010
 */
public interface ServiceResult {

    public String getEntry();

    public boolean isSuccess();
}
