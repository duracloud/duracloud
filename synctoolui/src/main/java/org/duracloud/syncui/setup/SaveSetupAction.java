/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.duracloud.syncui.controller.ConfigurationController.UpdatePolicy;
import org.duracloud.syncui.domain.AdvancedForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.util.UpdatePolicyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Component
public class SaveSetupAction {

    private static Logger log = LoggerFactory.getLogger(SaveSetupAction.class);
    private SyncConfigurationManager syncConfigurationManager;

    @Autowired
    public SaveSetupAction(
        @Qualifier("syncConfigurationManager") SyncConfigurationManager syncConfigurationManager) {
        this.syncConfigurationManager = syncConfigurationManager;
    }

    public String execute(DuracloudCredentialsForm credentials,
                          SpaceForm spaceForm,
                          DirectoryConfigs configs,
                          AdvancedForm advancedForm) {
        syncConfigurationManager.persistDuracloudConfiguration(credentials.getUsername(),
                                                               credentials.getPassword(),
                                                               credentials.getHost(),
                                                               credentials.getPort(),
                                                               spaceForm.getSpaceId());
        syncConfigurationManager.persistDirectoryConfigs(configs);

        syncConfigurationManager.setSyncDeletes(advancedForm.isSyncDeletes());
        String up = advancedForm.getUpdatePolicy();
        log.debug("setting update policy to  {}", up);
        UpdatePolicyHelper.set(this.syncConfigurationManager, UpdatePolicy.valueOf(up));
        syncConfigurationManager.setJumpStart(advancedForm.isJumpStart());
        
        log.info("successfully saved setup.");
        return "success";
    }
}
