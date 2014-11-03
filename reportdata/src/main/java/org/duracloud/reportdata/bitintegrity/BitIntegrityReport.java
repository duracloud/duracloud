/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.bitintegrity;

import java.io.InputStream;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class BitIntegrityReport {
    private InputStream stream;
    private BitIntegrityReportProperties properties;

    
    public BitIntegrityReport(InputStream stream,
                              BitIntegrityReportProperties properties) {
        super();
        this.stream = stream;
        this.properties = properties;
    }

    public BitIntegrityReportProperties getProperties() {
        return properties;
    }
    
    public InputStream getStream() {
        return stream;
    }
}
