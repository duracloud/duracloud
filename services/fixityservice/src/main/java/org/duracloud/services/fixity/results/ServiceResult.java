/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import java.util.Collection;

/**
 * This interface is for result objects in the ServiceResultProcessing
 * framework.
 *
 * @author Andrew Woods
 *         Date: Aug 4, 2010
 */
public interface ServiceResult {

    public static final char DELIM = '\t';

    public static final String HEADER =
        "space-id" + DELIM + "content-id" + DELIM + "MD5";

    public String getEntry();

    public String getHeader();

    public boolean isSuccess();
    
    public Collection<ServiceResultItem> getItems();
}
