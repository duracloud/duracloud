/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.replication;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.duracloud.services.hadoop.base.InitParamParser;
import org.duracloud.storage.domain.HadoopTypes;

import static org.duracloud.storage.domain.HadoopTypes.*;

/**
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepInitParamParser extends InitParamParser {

    /**
     * {@inheritDoc}
     */
    protected Options createOptions() {
        Options options = super.createOptions();

        String fromSpaceDesc = "The DuraCloud space from which files will be " +
                               "replicated";
        Option fromSpaceOption =
            new Option("s", TASK_PARAMS.SOURCE_SPACE_ID.getLongForm(), true, fromSpaceDesc);
        fromSpaceOption.setRequired(true);
        options.addOption(fromSpaceOption);

        String repStoreDesc = "The DuraCloud store to which files will be " +
                             "replicated";
        Option repStoreOption = new Option("t", TASK_PARAMS.REP_STORE_ID.getLongForm(), true, repStoreDesc);
        repStoreOption.setRequired(true);
        options.addOption(repStoreOption);

        String repSpaceDesc = "The DuraCloud space to which files will be " +
                              "replicated";
        Option repSpaceOption = new Option("e", TASK_PARAMS.REP_SPACE_ID.getLongForm(), true, repSpaceDesc);
        repSpaceOption.setRequired(true);
        options.addOption(repSpaceOption);        

        return options;
    }
}
