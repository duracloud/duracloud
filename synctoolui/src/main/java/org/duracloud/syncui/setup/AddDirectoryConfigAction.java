/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.setup;

import org.apache.commons.lang.StringUtils;
import org.duracloud.syncui.domain.DirectoryConfig;
import org.duracloud.syncui.domain.DirectoryConfigForm;
import org.duracloud.syncui.domain.DirectoryConfigs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Component
public class AddDirectoryConfigAction {

    private static Logger log =
        LoggerFactory.getLogger(AddDirectoryConfigAction.class);

    public void execute(DirectoryConfigForm form, DirectoryConfigs directoryConfigs) throws Exception {
        String path = form.getDirectoryPath();
        if(StringUtils.isNotBlank(path)){
            log.debug("adding selected directory {} to list", form.getDirectoryPath());
            directoryConfigs.add(new DirectoryConfig(path));
        }else{
            log.debug("the directory path is blank; ignoring...");
        }
    }
}
