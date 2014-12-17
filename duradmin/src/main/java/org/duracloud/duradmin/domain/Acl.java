/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.domain;

public class Acl {
    public static final String PUBLIC_GROUP = "group-public";
    public boolean read;
    public boolean write; 
    public String name;
    public String displayName;

    public Acl(String name) {
        this(name, name, false, false);
    }

    public Acl(String name, String displayName, boolean read, boolean write) {
        this.name = name;
        this.displayName = displayName;
        this.read = read;
        this.write = write;
    }

    public boolean isPublicGroup(){
        return this.name.equals(PUBLIC_GROUP);
    }
    
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
