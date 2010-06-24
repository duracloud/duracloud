/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

/**
 * This class is the top-level Runtime DuraCloud exception from which other
 * internal exceptions extend.
 *
 * @author Andrew Woods
 *         Date: Nov 20, 2009
 */
public class DuraCloudRuntimeException extends RuntimeException implements MessageFormattable {

    private MessageFormattable core;

    public DuraCloudRuntimeException() {
        super();
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudRuntimeException(String message) {
        super(message);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudRuntimeException(String message, String key) {
        super(message);
        core = new DuraCloudExceptionCore(key);
    }

    public DuraCloudRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudRuntimeException(String message,
                                     Throwable throwable,
                                     String key) {
        super(message, throwable);
        core = new DuraCloudExceptionCore(key);
    }

    public DuraCloudRuntimeException(Throwable throwable) {
        super(throwable);
        core = new DuraCloudExceptionCore();
    }

    public DuraCloudRuntimeException(Throwable throwable, String key) {
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
        }
        return msg;
    }
}
