/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.duracloud.sync.config.SyncToolConfig;
import org.duracloud.syncui.config.SyncUIConfig;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Component("syncConfigurationManager")
public class SyncConfigurationManagerImpl implements SyncConfigurationManager {
    private static Logger log =
        LoggerFactory.getLogger(SyncConfigurationManagerImpl.class);
    private SyncToolConfig syncToolConfig;
    private String configXmlPath;

    public SyncConfigurationManagerImpl() {
        String configPath = SyncUIConfig.getConfigPath();
        setConfigXmlPath(configPath);

        initializeSyncToolConfig();
    }

    private void persistSyncToolConfig() throws RuntimeException {
        try {
            SyncToolConfigSerializer.serialize(syncToolConfig,
                                               getSyncToolConfigXmlPath());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initializeSyncToolConfig() {
        try {
            this.syncToolConfig =
                SyncToolConfigSerializer.deserialize(getSyncToolConfigXmlPath());
        } catch (IOException ex) {
            log.warn("unable to deserialize sync config : " + ex.getMessage());
            log.info("creating new config...");
            this.syncToolConfig = new SyncToolConfig();
            initializeDefaultValues();
        }

    }

    private void initializeDefaultValues() {
        this.syncToolConfig.setContext("durastore");
        this.syncToolConfig.setExitOnCompletion(false);
        this.syncToolConfig.setSyncDeletes(false);
        List<File> dirs = new ArrayList<File>();
        this.syncToolConfig.setContentDirs(dirs);
        this.syncToolConfig.setMaxFileSize(SyncConfigurationManager.GIGABYTES);
    }

    private String getSyncToolConfigXmlPath() {
        return this.configXmlPath;
    }

    @Override
    public void persistDuracloudConfiguration(String username,
                                              String password,
                                              String host,
                                              String port,
                                              String spaceId) {
        this.syncToolConfig.setUsername(username);
        this.syncToolConfig.setPassword(password);
        this.syncToolConfig.setHost(host);
        this.syncToolConfig.setPort(Integer.parseInt(port));
        
        this.syncToolConfig.setSpaceId(spaceId);
        persistSyncToolConfig();

    }

    @Override
    public DuracloudConfiguration retrieveDuracloudConfiguration() {
        SyncToolConfig s = this.syncToolConfig;
        return new DuracloudConfiguration(s.getUsername(),
                                          s.getPassword(),
                                          s.getHost(),
                                          s.getPort(),
                                          s.getSpaceId());
    }

    @Override
    public DirectoryConfigs retrieveDirectoryConfigs() {
        DirectoryConfigs c = new DirectoryConfigs();
        List<File> dirs = this.syncToolConfig.getContentDirs();
        for (File f : dirs) {
            c.add(new DirectoryConfig(f.getAbsolutePath()));
        }
        return c;
    }

    @Override
    public void persistDirectoryConfigs(DirectoryConfigs configs) {
        List<File> dirs = new LinkedList<File>();

        for (DirectoryConfig f : configs) {
            dirs.add(new File(f.getDirectoryPath()));
        }
        this.syncToolConfig.setContentDirs(dirs);
        persistSyncToolConfig();
    }

    @Override
    public boolean isConfigurationComplete() {
        SyncToolConfig c = this.syncToolConfig;
        if (c == null) {
            return false;
        }

        if (StringUtils.isBlank(c.getUsername())
            || StringUtils.isBlank(c.getUsername())
            || StringUtils.isBlank(c.getPassword())
            || StringUtils.isBlank(c.getHost())
            || StringUtils.isBlank(c.getSpaceId())
            ) {
            return false;

        }

        return true;
    }

    @Override
    public void setConfigXmlPath(String configXml) {
        if(this.configXmlPath != configXml){
            this.configXmlPath = configXml;
            initializeSyncToolConfig();
        }
        log.info("xml config path set to {}", this.configXmlPath);
        
    }

    @Override
    public void persist() {
        persistSyncToolConfig();
    }

    @Override
    public File getWorkDirectory() {
        return SyncUIConfig.getWorkDir();
    }

    @Override
    public void purgeWorkDirectory() {
        try {
            FileUtils.cleanDirectory(SyncUIConfig.getWorkDir());
        } catch(IOException e) {
            log.error("Unable to clean work directory due to: " +
                      e.getMessage());
        }
    }
    
    @Override
    public boolean isSyncDeletes() {
        return this.syncToolConfig.syncDeletes();
    }
    
    @Override
    public void setSyncDeletes(boolean flag) {
        if(flag && this.syncToolConfig.isRenameUpdates()){
            //sync deletes cannot be used if rename updates is enabled.
            return;
        }
        this.syncToolConfig.setSyncDeletes(flag);
        persistSyncToolConfig();
    }
    
    @Override
    public String getUpdateSuffix() {
        return this.syncToolConfig.getUpdateSuffix();
    } 
    
    @Override
    public void setSyncUpdates(boolean b) {
        this.syncToolConfig.setSyncUpdates(b);
        persistSyncToolConfig();
    }
    
    @Override
    public boolean isSyncUpdates() {
        return this.syncToolConfig.isSyncUpdates();
    }

    @Override
    public void setRenameUpdates(boolean b) {
        if(b && this.syncToolConfig.syncDeletes()){
            //rename updates cannot be used if syncDeletes is enabled.
            return;
        }
        this.syncToolConfig.setRenameUpdates(b);
        persistSyncToolConfig();
    }

    @Override
    public boolean isRenameUpdates() {
        return this.syncToolConfig.isRenameUpdates();
    }

    @Override
    public String getPrefix() {
        return this.syncToolConfig.getPrefix();
    }
    
    
    @Override
    public void setPrefix(String prefix) {
        this.syncToolConfig.setPrefix(prefix);
        persistSyncToolConfig();
    }
    
    @Override
    public int getThreadCount() {
        return this.syncToolConfig.getNumThreads();
        
    }
    
    @Override
    public void setThreadCount(int threadCount) {
        this.syncToolConfig.setNumThreads(threadCount);
        persistSyncToolConfig();
    }

    @Override
    public void setJumpStart(boolean jumpStart) {
        if((jumpStart && !this.syncToolConfig.isSyncUpdates()) ||
           (jumpStart && this.syncToolConfig.isRenameUpdates())) {
            // Jump-start requires updates to be synced as overwrites
            return;
        }
        this.syncToolConfig.setJumpStart(jumpStart);
        persistSyncToolConfig();
    }

    @Override
    public boolean isJumpStart() {
        return this.syncToolConfig.isJumpStart();
    }
    
    @Override
    public RunMode getMode() {
        return this.syncToolConfig.exitOnCompletion()
            ? RunMode.SINGLE_PASS : RunMode.CONTINUOUS;
    }
    
    @Override
    public void setMode(RunMode mode) {
        RunMode oldValue = getMode();
        RunMode newValue = mode;
        
        if(oldValue != newValue){
            this.syncToolConfig.setExitOnCompletion(newValue.equals(RunMode.SINGLE_PASS) ? true : false);
            persistSyncToolConfig();
        }
    }
    
    @Override
    public void setMaxFileSizeInBytes(long maxFileSize) {
        int gigs =(int)(maxFileSize/SyncConfigurationManager.GIGABYTES);
        if(maxFileSize % SyncConfigurationManager.GIGABYTES != 0  || gigs < 1  || gigs > 5 ){
            throw new RuntimeException("Max file size must be divisible by 1000 and between 1 and 5 GBs inclusive");
        }
        long oldValue = getMaxFileSizeInBytes();
        if(oldValue != maxFileSize){
            this.syncToolConfig.setMaxFileSize(maxFileSize);
            persistSyncToolConfig();
        }
        
    }
    
    @Override
    public long getMaxFileSizeInBytes() {
         return this.syncToolConfig.getMaxFileSize();
    }
}
