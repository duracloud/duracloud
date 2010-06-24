/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * Determines the MIME type of a file. This is currently not a very
 * robust implementation. Using this method the mime type is determined
 * based on the file extension, and the mapping comes from the file
 * content-types.properties under the lib/ directory of the running JRE
 *
 * @author: Bill Branan
 * Date: May 7, 2010
 */
public class MimetypeUtil {

    private static final String DEFAULT_MIMETYPE = "application/octet-stream";

    private FileNameMap fileNameMap = URLConnection.getFileNameMap();

    public String getMimeType(File file) {
        if(file == null) {
            return DEFAULT_MIMETYPE;
        } else {
            return getMimeType(file.getName());
        }
    }

    public String getMimeType(String fileName) {
        if(fileName == null) {
            return DEFAULT_MIMETYPE;
        }

        String mimetype = fileNameMap.getContentTypeFor(fileName);        
        if(mimetype == null) {
            return DEFAULT_MIMETYPE;
        } else {
            return mimetype;   
        }
    }

}
