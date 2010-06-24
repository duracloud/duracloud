/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util.metrics;

public class MetricException
        extends Exception {

    private static final long serialVersionUID = 1L;

    public MetricException(String msg) {
        super(msg);
    }

}
