/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.beans.PropertyChangeEvent;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public interface SyncConfigurationChangeListener {
    public void configurationChanged(PropertyChangeEvent event);
}
