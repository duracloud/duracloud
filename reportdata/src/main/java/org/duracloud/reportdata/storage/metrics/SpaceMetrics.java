/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.metrics;

import java.util.List;

/**
 * Metrics data structure for spaces. Contains all of the metrics for a single
 * space.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class SpaceMetrics extends Metrics {

    private String spaceName;

    public SpaceMetrics(String spaceName,
                        long totalItems,
                        long totalSize,
                        List<MimetypeMetrics> mimetypeMetrics) {
        super(totalItems, totalSize, mimetypeMetrics);
        this.spaceName = spaceName;
    }

    public String getSpaceName() {
        return spaceName;
    }
    
}
