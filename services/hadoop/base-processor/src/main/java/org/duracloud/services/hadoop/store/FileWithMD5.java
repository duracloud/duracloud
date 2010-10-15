/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Oct 14, 2010
 */
public class FileWithMD5 {
    private File file;
    private String md5;

    public FileWithMD5(File file, String md5) {
        if (null == file) {
            throw new RuntimeException("Can not construct with null file!");
        }

        this.file = file;
        this.md5 = md5;
    }

    public File getFile() {
        return file;
    }

    public String getMd5() {
        return md5;
    }
}
