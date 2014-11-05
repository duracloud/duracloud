/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import java.io.Serializable;

import org.duracloud.syncui.controller.ConfigurationController.UpdatePolicy;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Component
public class AdvancedForm implements Serializable{
    private String updatePolicy = UpdatePolicy.OVERWRITE.name();
    private boolean syncDeletes;
    private boolean jumpStart;

    public boolean isSyncDeletes() {
        return syncDeletes;
    }
    
    public void setSyncDeletes(boolean syncDeletes) {
        this.syncDeletes = syncDeletes;
    }
    
    public void setUpdatePolicy(String updatePolicy) {
        this.updatePolicy = updatePolicy;
    }
    
    public String getUpdatePolicy() {
        return updatePolicy;
    }

    public boolean isJumpStart() {
        return jumpStart;
    }

    public void setJumpStart(boolean jumpStart) {
        this.jumpStart = jumpStart;
    }
}
