/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;


/**
 * A list of configured directories.
 * @author Daniel Bernstein
 *
 */
@Component("directoryConfigs")
public class DirectoryConfigs extends LinkedHashSet<DirectoryConfig>{

    private static final long serialVersionUID = 1L;

    public List<File> toFileList() {
        List<File> list = new LinkedList<File>();
        
        for(DirectoryConfig config : this){
            list.add(new File(config.getDirectoryPath()));
        }
        return list;
    }

    public DirectoryConfig removePath(String path) {
        for(DirectoryConfig d : this){
            if(d.getDirectoryPath().equals(path)){
                this.remove(d);
                return d;
            }
        }
        
        return null;
    }
    

}
