/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author Andrew Woods
 *         Date: 3/28/12
 */
public class ManifestArgumentException extends DuraCloudCheckedException {

    public ManifestArgumentException(String msg, Throwable e) {
        super(msg, e);
    }

    public ManifestArgumentException(String msg) {
        super(msg);
    }
}
