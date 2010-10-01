/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import java.io.File;

/**
 * Stores the result of file processing. The file should be the resultant file,
 * the contentId should be the ID under which the resultant file will be stored.
 *
 * @author: Bill Branan
 * Date: Oct 1, 2010
 */
public class ProcessResult {

    private File file;
    private String contentId;

    public ProcessResult(File file, String contentId) {
        this.file = file;
        this.contentId = contentId;
    }

    public File getFile() {
        return file;
    }

    public String getContentId() {
        return contentId;
    }

}
