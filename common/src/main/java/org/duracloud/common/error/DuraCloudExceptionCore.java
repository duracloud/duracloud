/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

import java.text.MessageFormat;
import java.io.Serializable;


/**
 * This class is the core utility for supporting DuraCloud exceptions and
 * user-friendly formatted messages.
 *
 * @author Andrew Woods
 *         Date: Nov 20, 2009
 */
public class DuraCloudExceptionCore implements MessageFormattable, Serializable {

    private String key = "duracloud.error.general";
    private String[] args;

    public DuraCloudExceptionCore() {
        super();
    }

    public DuraCloudExceptionCore(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String[] getArgs() {
        if (args == null) {
            args = new String[0];
        }
        return args;
    }

    public void setArgs(String... args) {
        this.args = args;
    }

    public String getFormattedMessage() {
        String pattern = null;
        try {
            pattern = ExceptionMessages.getMessagePattern(getKey());
        } catch (Exception e) {
            // do nothing
        }

        if (pattern == null) {
            return null;
        } else {
            return MessageFormat.format(pattern, getArgs());
        }
    }

}
