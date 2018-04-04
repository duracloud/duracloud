/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.config;

/**
 * Configuration of the Sync Optimizer.
 *
 * @author Bill Branan
 * Date: 5/16/14
 */
public class SyncOptimizeConfig {

    private String host;
    private int port;
    private String context;
    private String username;
    private String password;
    private String spaceId;
    private String version;
    private int numFiles;
    private int sizeFiles;

    public String getPrintableConfig() {
        StringBuilder config = new StringBuilder();

        config.append("\n---------------------------------------------\n");
        config.append(" Sync Thread Optimizer " + version + " - Configuration");
        config.append("\n---------------------------------------------\n");

        config.append("DuraStore Host: ");
        config.append(getHost()).append("\n");
        config.append("DuraStore Port: ");
        config.append(getPort()).append("\n");
        config.append("DuraStore Username: ");
        config.append(getUsername()).append("\n");
        config.append("DuraStore Space ID: ");
        config.append(getSpaceId()).append("\n");
        config.append("Number of files: ");
        config.append(getNumFiles()).append("\n");
        config.append("Size of files: ");
        config.append(getSizeFiles()).append(" MB\n");

        config.append("--------------------------------------\n");

        return config.toString();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(int numFiles) {
        this.numFiles = numFiles;
    }

    public int getSizeFiles() {
        return sizeFiles;
    }

    public void setSizeFiles(int sizeFiles) {
        this.sizeFiles = sizeFiles;
    }

}
