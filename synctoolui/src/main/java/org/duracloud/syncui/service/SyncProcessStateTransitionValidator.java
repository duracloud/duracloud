/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.duracloud.syncui.domain.SyncProcessState;

/**
 * This class encapsulates the state change rules for the SyncProcessManager.
 * 
 * @author Daniel Bernstein
 * 
 */
public class SyncProcessStateTransitionValidator {
    /**
     * 
     * @param currentState
     * @param newState
     * @return true if the state transition is valid
     */
    public boolean validate(SyncProcessState from, SyncProcessState to) {
        if (from == SyncProcessState.STOPPED) {
            if (to == SyncProcessState.STARTING) {
                return true;
            }
        } else if (from == SyncProcessState.STARTING) {
            if (to == SyncProcessState.RUNNING || to == SyncProcessState.STOPPING || to == SyncProcessState.ERROR) {
                return true;
            }
        } else if (from == SyncProcessState.RUNNING) {
            if (to == SyncProcessState.STOPPING || to == SyncProcessState.PAUSING ||  to == SyncProcessState.ERROR) {
                return true;
            }
        } else if (from == SyncProcessState.STOPPING) {
            if (to == SyncProcessState.STOPPED || to == SyncProcessState.ERROR) {
                return true;
            }
        } else if (from == SyncProcessState.PAUSING) {
            if (to == SyncProcessState.PAUSED || to == SyncProcessState.ERROR) {
                return true;
            }
        } else if (from == SyncProcessState.PAUSED) {
            if (to == SyncProcessState.RESUMING || to == SyncProcessState.ERROR || 
                    to == SyncProcessState.STOPPING) {
                return true;
            }
        } else if (from == SyncProcessState.RESUMING) {
            if (to == SyncProcessState.RUNNING || to == SyncProcessState.ERROR) {
                return true;
            }
        }

        return false;
    }
}
