/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.duracloud.common.error.NoUserLoggedInException;

/**
 * @author Bill Branan
 *         Date: 3/17/14
 */
public interface UserUtil {

    public String getCurrentUsername() throws NoUserLoggedInException;

}
