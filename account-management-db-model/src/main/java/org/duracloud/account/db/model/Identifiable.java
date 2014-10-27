/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

/**
 * @author Erik Paulsson
 *         Date: 7/10/13
 */
public interface Identifiable {
    public Long getId();
    public void setId(Long id);
}
