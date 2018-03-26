/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.retry;

/**
 * All implementations of this interface perform an action which may need to be
 * attempted more than once to allow for a greater likelihood of success.
 *
 * @author Bill Branan
 * Date: 10/23/13
 */
public interface Retriable {

    Object retry() throws Exception;

}
