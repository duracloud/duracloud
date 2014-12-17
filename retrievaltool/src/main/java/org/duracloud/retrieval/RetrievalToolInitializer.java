/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval;

import org.duracloud.retrieval.config.RetrievalToolConfig;
import org.duracloud.retrieval.config.RetrievalToolConfigParser;

/**
 * @author Bill Branan
 *         Date: 10/16/14
 */
public class RetrievalToolInitializer {

    public static void main(String[] args) {
        RetrievalToolInitializer initializer = new RetrievalToolInitializer();
        initializer.runRetrievalTool(args);
    }

    public void runRetrievalTool(String[] args) {
        // Parse command line options.
        RetrievalToolConfigParser syncConfigParser =
            new RetrievalToolConfigParser();
        RetrievalToolConfig retConfig =
            syncConfigParser.processCommandLine(args);

        // Start up the RetrievalTool
        RetrievalTool retrievalTool = new RetrievalTool();
        retrievalTool.setRetrievalConfig(retConfig);
        retrievalTool.runRetrievalTool();
    }

}
