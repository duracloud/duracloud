/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import java.io.Serializable;

/**
 * This class holds the description and state of a service deployment host option.
 *
 * @author Andrew Woods
 *         Date: Nov 6, 2009
 */
public class DeploymentOption implements Serializable {

    private static final long serialVersionUID = -5554753103296039413L;

    /**
     * Is this the primary host? new one? existing secondary host?
     */
    public enum Location {
        PRIMARY, NEW, EXISTING;
    }

    public enum State {
        AVAILABLE, UNAVAILABLE;
    }

    private String displayName;
    private Location location;
    private String hostname;
    private State state;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Location getLocationType() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeploymentOption)) {
            return false;
        }

        DeploymentOption that = (DeploymentOption) o;

        if (displayName != null ? !displayName.equals(that.displayName) :
            that.displayName != null) {
            return false;
        }
        if (hostname != null ? !hostname.equals(that.hostname) :
            that.hostname != null) {
            return false;
        }
        if (location != that.location) {
            return false;
        }
        if (state != that.state) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName != null ? displayName.hashCode() : 0;
        result =
            31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (hostname != null ? hostname.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }
    
    public String toString(){
        return "DeploymentOption([displayName="+this.displayName + ", hostname=" + this.hostname + ", location=" + this.location + ", state="+this.state+"])";
    }
}