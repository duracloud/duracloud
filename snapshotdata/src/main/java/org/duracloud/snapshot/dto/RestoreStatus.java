/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto;

public enum RestoreStatus {
    INITIALIZED,
    WAITING_FOR_DPN,
    DPN_TRANSFER_COMPLETE,
    VERIFYING_DPN_TO_BRIDGE_TRANSFER,
    VERIFYING_SNAPSHOT_REPO_AGAINST_MANIFEST,
    TRANSFERRING_TO_DURACLOUD,
    TRANSFER_TO_DURACLOUD_COMPLETE,
    VERIFYING_TRANSFERRED_CONTENT,
    CLEANING_UP,
    ERROR,
    RESTORATION_COMPLETE,
    RESTORATION_EXPIRED,
    CANCELLED;
}
