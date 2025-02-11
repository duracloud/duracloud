/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import java.io.File;

/**
 * @author Nicholas Woodward
 */
public class ExcludeForm {
    private String excludeListPath;
    private File excludeList;

    public String getExcludeListPath() {
        return excludeListPath;
    }

    public void setExcludeListPath(String excludeListPath) {
        this.excludeListPath = excludeListPath;
    }

    public File getExcludeList() {
        return excludeList;
    }

    public void setExcludeList(File excludeList) {
        this.excludeList = excludeList;
    }
}
