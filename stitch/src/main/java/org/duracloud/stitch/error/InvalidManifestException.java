/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.error;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author Andrew Woods
 *         Date: 9/2/11
 */
public class InvalidManifestException extends DuraCloudCheckedException {


    public InvalidManifestException(String spaceId, String contentId) {
        super("Invalid manifest name: " + spaceId + "/" + contentId +
                  ". Should end with suffix: " + ChunksManifest.manifestSuffix);
    }

    public InvalidManifestException(String spaceId,
                                    String contentId,
                                    String msg) {
        super(
            "Error with manifest: " + spaceId + "/" + contentId + ", due to: " +
                msg);
    }

    public InvalidManifestException(String spaceId,
                                    String contentId,
                                    String msg,
                                    Exception e) {
        super("Error getting manifest: " + spaceId + "/" + contentId +
                  ", due to: " + msg, e);
    }
}
