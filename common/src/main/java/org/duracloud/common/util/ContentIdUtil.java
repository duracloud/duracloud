/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.File;
import java.net.URI;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ContentIdUtil {

    /**
     * Determines the content ID of a file: the path of the file relative to
     * the watched directory. If the watched directory is null, the content ID
     * is simply the name of the file.
     *
     * If a prefix is being used, the prefix is added as the initial characters
     * in the contentId.
     * 
     * @param file
     * @param watchDir
     * @param contentIdPrefix
     * @return
     */
    public static String getContentId(File file,
                                      File watchDir,
                                      String contentIdPrefix) {
        String contentId = file.getName();
        if(null != watchDir) {
            URI relativeFileURI = watchDir.toURI().relativize(file.toURI());
            contentId = relativeFileURI.getPath();
        }
        if(null != contentIdPrefix) {
            contentId = contentIdPrefix + contentId;
        }
        return contentId;
    }

}
