/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;


/**
 * This interface is for describing fine grained details of service results.
 * framework.
 *
 * @author Daniel Bernstein
 *         Date: Jan 12, 2012
 */
public interface ServiceResultItem {
    public String getEntry();
    public boolean isSuccess();
}
