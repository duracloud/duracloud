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
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.domain.DuracloudCredentialsForm;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.duracloud.syncui.AbstractTest;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class SaveSetupActionTest extends AbstractTest {

    @Test
    public void testExecute() throws Exception {
        DirectoryConfigs configs = new DirectoryConfigs();
        configs.add(new DirectoryConfig("/test"));
        SpaceForm space = new SpaceForm();

        SyncConfigurationManager scm =
            createMock(SyncConfigurationManager.class);
        
        String username = "username";
        String password = "password";
        String host = "host";
        String spaceId = "spaceId";
        DuracloudCredentialsForm cred = new DuracloudCredentialsForm();
        cred.setUsername(username);
        cred.setPassword(password);
        cred.setHost(host);
        space.setSpaceId(spaceId);
        
        scm.persistDuracloudConfiguration(username, password, host, cred.getPort(), spaceId);
        EasyMock.expectLastCall();
        scm.persistDirectoryConfigs(configs);


        AdvancedForm advanced = new AdvancedForm();
        advanced.setSyncDeletes(false);
        scm.setSyncDeletes(advanced.isSyncDeletes());
        EasyMock.expectLastCall();
        advanced.setJumpStart(false);
        scm.setJumpStart(advanced.isJumpStart());
        EasyMock.expectLastCall();

        advanced.setUpdatePolicy(UpdatePolicy.OVERWRITE.name());
        scm.setSyncUpdates(true);
        EasyMock.expectLastCall();
        scm.setRenameUpdates(false);
        EasyMock.expectLastCall();
        
        SaveSetupAction sc = new SaveSetupAction(scm);
        replay();

        String result = sc.execute(cred, space, configs, advanced);

        Assert.assertEquals("success", result);

    }

}
