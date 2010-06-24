/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.metrics;

/**
 * This interface allows implementations to be injected with a MetricsTable in
 * which to collect Metrics.
 *
 * @author Andrew Woods
 */
public interface MetricsProbed {

    public void setMetricsTable(MetricsTable metricsTable);

}
