/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.manifest.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 *         Date: 3/29/12
 */
public class ManifestGeneratorException extends DuraCloudRuntimeException {

    public ManifestGeneratorException(String msg, Throwable e) {
        super(msg, e);
    }

    public ManifestGeneratorException(String msg) {
        super(msg);
    }
}
