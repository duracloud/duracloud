/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.bulk.ManifestVerifier;
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.postprocessing.PassFailPostJobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class detects if errors exist in lines of fixity service reports.
 *
 * @author Andrew Woods
 *         Date: 6/9/11
 */
public class FixityPassFailPostJobWorker extends PassFailPostJobWorker {

    private final Logger log = LoggerFactory.getLogger(
        FixityPassFailPostJobWorker.class);

    public FixityPassFailPostJobWorker(AmazonMapReduceJobWorker predecessor,
                                       ContentStore contentStore,
                                       String serviceWorkDir,
                                       String spaceId,
                                       String contentId,
                                       String errorReportContentId) {
        super(predecessor, contentStore, serviceWorkDir, spaceId, contentId, errorReportContentId);
    }

    @Override
    protected boolean isCompleteFailure(String line) {
        return(line.toLowerCase().startsWith("error") &&
                !line.contains(ComputeService.DELIM +""));
    }
    
    @Override
    protected boolean isError(String line) {
        // null is ok
        if (null == line) {
            return false;
        }

        // whitespace lines are ok
        String text = line.replaceAll("\\s", "");
        if (text.length() == 0) {
            return false;
        }

        // VALID is ok
        if (text.contains(ManifestVerifier.State.VALID.name())) {
            return false;
        }

        log.warn("Error line: {}", line);

        // everything else is an error.
        return true;
    }
}
