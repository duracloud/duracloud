/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public interface ChangedListListener {
    public void listChanged(ChangedList list);
}
