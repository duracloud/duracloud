/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.duracloud.syncui.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class AddDirectoryConfigActionTest  extends AbstractTest {
    
    @Test
    public void testExecute() throws Exception {
        AddDirectoryConfigAction action = new AddDirectoryConfigAction();
        DirectoryConfigForm form = new DirectoryConfigForm();
        form.setDirectoryPath("test");
        DirectoryConfigs configs = new DirectoryConfigs();
        replay();
        
        action.execute(form, configs);
        Assert.assertEquals(1, configs.size());

    }

}
