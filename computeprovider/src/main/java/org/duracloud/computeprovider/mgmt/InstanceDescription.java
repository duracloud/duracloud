/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt;

import java.net.URL;

import java.util.Date;

public abstract class InstanceDescription {

    protected URL url;

    protected String provider;

    protected String instanceId;

    protected InstanceState state;

    protected Date launchTime;

    protected Exception exception;

    public boolean hasError() {
        return exception != null;
    }

    public URL getURL() {
        return url;
    }

    public String getProvider() {
        return provider;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public InstanceState getState() {
        return state;
    }

    public Date getLaunchTime() {
        return launchTime;
    }

    public Exception getException() {
        return exception;
    }

}
