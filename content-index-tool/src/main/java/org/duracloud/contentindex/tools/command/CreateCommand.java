/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.tools.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.duracloud.common.cli.Command;
import org.duracloud.contentindex.client.ESContentIndexInitializer;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class CreateCommand extends Command {
    public CreateCommand() {
        super("init-shared-index");
    }

    @Override
    protected Options createOptions() {
        return new Options();
    }

    @Override
    protected void executeImpl(String[] args, CommandLine cl) {
        ESContentIndexInitializer initializer =
            new ESContentIndexInitializer();
        initializer.initialize();
    }
}