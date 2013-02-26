/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.apache.commons.collections.CollectionUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            || CollectionUtils.isEmpty(c.getContentDirs())) {
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

    File getWorkDirectory() {
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

}
