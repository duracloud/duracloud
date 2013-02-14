/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import java.io.File;
import java.io.Serializable;

/**
 * A configured directory.
 * 
 * @author Daniel Bernstein
 * 
 */
public class DirectoryConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String directoryPath;

    public DirectoryConfig(String directoryPath) {
        if (directoryPath == null){
            throw new NullPointerException("directoryPath must be non-null");
        }
        this.directoryPath = directoryPath;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }
    
    public File getFile(){
        return  new File(this.directoryPath);
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DirectoryConfig) {
            return ((DirectoryConfig) obj).directoryPath.equals(this.directoryPath);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return directoryPath.hashCode()*13;
    }

}
