/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.config;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Configuration for the Sync Tool
 *
 * @author: Bill Branan
 * Date: Mar 25, 2010
 */
public class SyncToolConfig implements Serializable {

    private String host;
    private int port;
    private String context;
    private String username;
    private String password;
    private String storeId;
    private String spaceId;
    private File workDir;
    private List<File> contentDirs;
    private long pollFrequency;
    private int numThreads;
    private long maxFileSize;
    private boolean syncDeletes;
    private boolean exitOnCompletion;
    private String version;

    public String getPrintableConfig() {
        StringBuilder config = new StringBuilder();

        config.append("\n--------------------------------------\n");
        config.append(" Sync Tool " + version + " - Configuration");
        config.append("\n--------------------------------------\n");

        config.append("Content Directories:\n");
        for(File dir : getContentDirs()) {
            config.append("  ").append(dir.getAbsolutePath()).append("\n");
        }

        config.append("DuraStore Host: ");
        config.append(getHost()).append("\n");
        config.append("DuraStore Port: ");
        config.append(getPort()).append("\n");
        config.append("DuraStore Username: ");
        config.append(getUsername()).append("\n");

        if(getStoreId() != null) {
            config.append("DuraCloud Store ID: ");
            config.append(getStoreId()).append("\n");
        }

        config.append("DuraCloud Space ID: ");
        config.append(getSpaceId()).append("\n");
        config.append("SyncTool Work Directory: ");
        config.append(getWorkDir()).append("\n");
        config.append("SyncTool Poll Frequency: ");
        config.append(getPollFrequency());
        config.append("\n");
        config.append("SyncTool Threads: ");
        config.append(getNumThreads()).append("\n");
        config.append("SyncTool Max File Size: ");
        config.append(getMaxFileSize()).append(" bytes\n");
        config.append("SyncTool Syncing Deletes: ");
        config.append(syncDeletes()).append("\n");
        config.append("SyncTool Exit on Completion: ");
        config.append(exitOnCompletion()).append("\n");
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

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        this.workDir = workDir;
    }

    public List<File> getContentDirs() {
        return contentDirs;
    }

    public void setContentDirs(List<File> contentDirs) {
        this.contentDirs = contentDirs;
    }

    public long getPollFrequency() {
        return pollFrequency;
    }

    public void setPollFrequency(long pollFrequency) {
        this.pollFrequency = pollFrequency;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public boolean syncDeletes() {
        return syncDeletes;
    }

    public void setSyncDeletes(boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }

    public boolean exitOnCompletion() {
        return exitOnCompletion;
    }

    public void setExitOnCompletion(boolean exitOnCompletion) {
        this.exitOnCompletion = exitOnCompletion;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
