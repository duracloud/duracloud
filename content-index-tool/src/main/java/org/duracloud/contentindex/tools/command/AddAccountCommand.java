/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.tools.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.duracloud.common.cli.Command;
import org.duracloud.contentindex.client.ContentIndexClient;
import org.duracloud.contentindex.client.ESContentIndexClientUtil;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AddAccountCommand extends Command {
    public AddAccountCommand() {
        super("add-account");
    }

    @Override
    protected Options createOptions() {
        Options options = new Options();
        options.addOption(new Option("a",
                                     "account",
                                     true,
                                     "Account name (ie subdomain)"));
        return options;
    }

    @Override
    protected void executeImpl(String[] args, CommandLine cl) {
        String account = cl.getOptionValue("a");
        if (account == null) {
            usage(args);

        }
        ContentIndexClient client =
            ESContentIndexClientUtil.createContentIndexClient();
        client.addIndex(account, true);
        return;

    }
}