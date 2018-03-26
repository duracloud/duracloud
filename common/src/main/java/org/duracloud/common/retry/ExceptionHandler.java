/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.retry;

/**
 * A simple interface for handling exceptions.
 *
 * @author Daniel Bernstein
 * Date: Dec 13, 2013
 */
public interface ExceptionHandler {
    void handle(Exception ex);
}
