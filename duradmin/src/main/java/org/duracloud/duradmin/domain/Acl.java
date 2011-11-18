package org.duracloud.duradmin.domain;

public class Acl {
    public boolean read;
    public boolean write; 
    public String name;
    public String displayName;

    public Acl(String name, String displayName, boolean read, boolean write) {
        this.name = name;
        this.displayName = displayName;
        this.read = read;
        this.write = write;
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