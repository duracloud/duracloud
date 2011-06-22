/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor;

/**
 * This interface collects service summary details and writes them back to
 * durastore.
 *
 * @author Andrew Woods
 *         Date: 6/17/11
 */
public interface ServiceSummaryWriter {

    /**
     * This method collects the service summary for the service defined by the
     * arg serviceId/deploymentId and writes it back to durastore.
     *
     * @param serviceId    of completed service
     * @param deploymentId of completed service
     */
    public void collectAndWriteSummary(int serviceId, int deploymentId);
}
