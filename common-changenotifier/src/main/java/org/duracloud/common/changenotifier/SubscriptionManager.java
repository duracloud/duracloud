/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

/**
 * @author Shibo Liu
 * Feb 29, 2020
 */
public interface SubscriptionManager {

    public void addListener(MessageListener listener);

    public void connect();

    public void disconnect();
}
