/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

import org.duracloud.common.util.ExceptionUtil;

/**
 * This class is the top-level Checked DuraCloud exception from which other
 * internal exceptions extend.
 *
 * @author Andrew Woods
 *         Date: Oct 24, 2009
 */
public class DuraCloudCheckedException extends Exception implements MessageFormattable {

    private MessageFormattable core;

    public DuraCloudCheckedException() {
        super();
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudCheckedException(String message) {
        super(message);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudCheckedException(String message, String key) {
        super(message);
        core = new DuraCloudExceptionCore(key);
    }

    public DuraCloudCheckedException(String message, Throwable throwable) {
        super(message, throwable);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudCheckedException(String message,
                                     Throwable throwable,
                                     String key) {
        super(message, throwable);
        core = new DuraCloudExceptionCore(key);
    }

    public DuraCloudCheckedException(Throwable throwable) {
        super(throwable);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudCheckedException(Throwable throwable, String key) {
        super(throwable);
        core = new DuraCloudExceptionCore(key);
    }

    public String getKey() {
        return core.getKey();
    }

    public String[] getArgs() {
        return core.getArgs();
    }

    public void setArgs(String... args) {
        core.setArgs(args);
    }

    public String getFormattedMessage() {
        String msg = core.getFormattedMessage();
        if (null == msg) {
            msg = this.getMessage();
            msg += ExceptionUtil.getStackTraceAsString(this);            
        }
        return msg;
    }
}
