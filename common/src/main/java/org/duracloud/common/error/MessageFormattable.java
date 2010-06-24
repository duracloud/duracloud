/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

/**
 * This interface defines the methods for creating formatted messages.
 *
 * @author Andrew Woods
 *         Date: Nov 20, 2009
 */
public interface MessageFormattable {

    /**
     * This method returns the message key used as an index in the ResourceBundle.
     * @return key
     */
    public String getKey();

    /**
     * This method returns the args that act as variables in the message.
     * @return list of message elements
     */
    public String[] getArgs();

    /**
     * This method sets the arg variables that are placed in the message.
     * @param args list of elements to be placed in message
     */
    public void setArgs(String... args);

    /**
     * This method returns the message which includes the arg elements.
     * @return formatted message
     */
    public String getFormattedMessage();
}
