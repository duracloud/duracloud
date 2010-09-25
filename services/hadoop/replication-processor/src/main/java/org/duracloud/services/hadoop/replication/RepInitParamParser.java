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

/**
 * @author: Bill Branan
 * Date: Sept 23, 2010
 */
public class RepInitParamParser extends InitParamParser {

    public static final String TO_STORE_ID = "toStoreId";
    public static final String TO_SPACE_ID = "toSpaceId";
    public static final String FROM_SPACE_ID = "fromSpaceId";
    public static final String DC_HOST = "dcHost";
    public static final String DC_PORT = "dcPort";
    public static final String DC_CONTEXT = "dcContext";
    public static final String DC_USERNAME = "dcUsername";
    public static final String DC_PASSWORD = "dcPassword";

    /**
     * {@inheritDoc}
     */
    protected Options createOptions() {
        Options options = super.createOptions();

        String toStoreDesc = "The DuraCloud store to which files will be " +
                             "replicated";
        Option toStoreOption = new Option("r", TO_STORE_ID, true, toStoreDesc);
        toStoreOption.setRequired(true);
        options.addOption(toStoreOption);

        String toSpaceDesc = "The DuraCloud space to which files will be " +
                             "replicated";
        Option toSpaceOption = new Option("s", TO_SPACE_ID, true, toSpaceDesc);
        toSpaceOption.setRequired(true);
        options.addOption(toSpaceOption);

        String fromSpaceDesc = "The DuraCloud space from which files will be " +
                               "replicated";
        Option fromSpaceOption =
            new Option("f", FROM_SPACE_ID, true, fromSpaceDesc);
        fromSpaceOption.setRequired(true);
        options.addOption(fromSpaceOption);        

        String dcHostDesc = "The host name used to connect to DuraCloud";
        Option dcHostOption = new Option("h", DC_HOST, true, dcHostDesc);
        dcHostOption.setRequired(true);
        options.addOption(dcHostOption);

        String dcPortDesc = "The port used to connect to DuraCloud";
        Option dcPortOption = new Option("t", DC_PORT, true, dcPortDesc);
        dcPortOption.setRequired(false);
        options.addOption(dcPortOption);

        String dcContextDesc = "The app context used to connect to DuraCloud";
        Option dcContextOption =
            new Option("c", DC_CONTEXT, true, dcContextDesc);
        dcContextOption.setRequired(false);
        options.addOption(dcContextOption);

        String dcUserDesc = "The username used to connect to DuraCloud";
        Option dcUserOption = new Option("u", DC_USERNAME, true, dcUserDesc);
        dcUserOption.setRequired(true);
        options.addOption(dcUserOption);

        String dcPassDesc = "The password used to connect to DuraCloud";
        Option dcPassOption = new Option("p", DC_PASSWORD, true, dcPassDesc);
        dcPassOption.setRequired(true);
        options.addOption(dcPassOption);

        return options;
    }
}
