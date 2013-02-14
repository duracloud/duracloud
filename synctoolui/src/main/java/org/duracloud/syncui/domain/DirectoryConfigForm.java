/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * This class backs the directory configuration form.
 * @author Daniel Bernstein
 *
 */
@Component("directoryConfigForm")
public class DirectoryConfigForm implements Serializable {

    private static final long serialVersionUID = 1L;

    private String directoryPath;

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

}
