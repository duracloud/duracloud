/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.util;

import org.duracloud.syncui.controller.ConfigurationController;
import org.duracloud.syncui.controller.ConfigurationController.UpdatePolicy;
import org.duracloud.syncui.service.SyncConfigurationManager;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class UpdatePolicyHelper {
    public static void set(SyncConfigurationManager scm, UpdatePolicy up){
        if(UpdatePolicy.NONE.equals(up)){
            scm.setSyncUpdates(false);
            scm.setRenameUpdates(false);
        }else if(UpdatePolicy.PRESERVE.equals(up)){
            scm.setSyncUpdates(true);
            scm.setRenameUpdates(true);
        }else if(UpdatePolicy.OVERWRITE.equals(up)){
            scm.setSyncUpdates(true);
            scm.setRenameUpdates(false);
        }else{
            throw new IllegalArgumentException("unknown update policy: " + up);
        }
    }
    
    public static UpdatePolicy get(SyncConfigurationManager scm){
        if(!scm.isSyncUpdates()){
            return UpdatePolicy.NONE;
        }else {
            if(!scm.isRenameUpdates()){
                return UpdatePolicy.OVERWRITE; 
            }else{
                return UpdatePolicy.PRESERVE;
            }
        }
    }

}
