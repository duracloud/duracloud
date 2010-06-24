/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.domain;

/**
 * Contains the information necessary to connect to a DuraCloud
 * store in order to access user contentn
 *
 * @author Bill Branan
 */
public class UserStore extends Store {

    private String msgBrokerUrl;

    public String getMsgBrokerUrl() {
        return msgBrokerUrl;
    }

    public void setMsgBrokerUrl(String msgBrokerUrl) {
        this.msgBrokerUrl = msgBrokerUrl;
    }
}