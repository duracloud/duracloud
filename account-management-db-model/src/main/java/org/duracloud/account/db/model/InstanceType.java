/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

/**
 * Represents different instance sizes available on which to deploy duracloud.
 *
 * @author Daniel Bernstein
 *
 */
public enum InstanceType {
    SMALL,
    MEDIUM,
    LARGE,
    XLARGE;
}
