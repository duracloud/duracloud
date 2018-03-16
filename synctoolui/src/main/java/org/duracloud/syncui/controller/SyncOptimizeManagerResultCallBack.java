/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

/**
 * Callback for asynchronous calls to the SyncOptimizeManager.
 *
 * @author Daniel Bernstein
 */
public interface SyncOptimizeManagerResultCallBack {
    void onSuccess();

    void onFailure(Exception ex, String status);
}
