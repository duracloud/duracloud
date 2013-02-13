/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.config;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Configuration for the Retrieval Tool
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class RetrievalToolConfig implements Serializable {

    private String host;
    private int port;
    private String context;
    private String username;
    private String password;
    private String storeId;
    private List<String> spaces;
    private boolean allSpaces;
    private File contentDir;
    private File workDir;
    private boolean overwrite;
    private int numThreads;
    private String version;
    private boolean applyTimestamps;

    public String getPrintableConfig() {
        StringBuilder config = new StringBuilder();

        config.append("\n--------------------------------------\n");
        config.append(" Retrieval Tool " + version + " - Configuration");
        config.append("\n--------------------------------------\n");

        if(allSpaces) {
            config.append("Retrieve all spaces: true");
        } else {
            config.append("Retrieve spaces:");
            for(String space : spaces) {
                config.append(" ").append(space);
            }
        }
        config.append("\n");

        config.append("DuraStore Host: ");
        config.append(getHost()).append("\n");
        config.append("DuraStore Port: ");
        config.append(getPort()).append("\n");
        config.append("DuraStore Username: ");
        config.append(getUsername()).append("\n");

        if(storeId != null) {
            config.append("DuraStore Store: ");
            config.append(getStoreId()).append("\n");
        }

        config.append("Retrieval Tool Content Directory: ");
        config.append(getContentDir().getAbsolutePath()).append("\n");
        config.append("Retrieval Tool Work Directory: ");
        config.append(getWorkDir().getAbsolutePath()).append("\n");
        config.append("Retrieval Tool Overwrite Local Files: ");
        config.append(isOverwrite()).append("\n");
        config.append("Retrieval Tool Retain File Time Stamps: ");
        config.append(isApplyTimestamps()).append("\n");

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

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public List<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(List<String> spaces) {
        this.spaces = spaces;
    }

    public boolean isAllSpaces() {
        return allSpaces;
    }

    public void setAllSpaces(boolean allSpaces) {
        this.allSpaces = allSpaces;
    }

    public File getContentDir() {
        return contentDir;
    }

    public void setContentDir(File contentDir) {
        this.contentDir = contentDir;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isApplyTimestamps() {
        return applyTimestamps;
    }

    public void setApplyTimestamps(boolean applyTimestamps) {
        this.applyTimestamps = applyTimestamps;
    }

}
