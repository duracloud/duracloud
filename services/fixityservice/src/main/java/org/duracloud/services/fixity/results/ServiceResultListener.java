/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.duracloud.services.fixity.results.ServiceResult;


/**
 * @author: Andrew Woods
 * Date: Aug 4, 2010
 */
public interface ServiceResultListener {

    public void processServiceResult(ServiceResult result);

    public String getProcessingStatus();

    public void setTotalWorkitems(long total);

    void setProcessingComplete();
}
