/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author: Bill Branan
 * Date: 7/8/11
 */
@XmlRootElement
public class StorageReportList extends StorageReportBase {

    @XmlElement(name = "storageReport")
    private List<String> storageReportList;

    // Required for JAXB
    private StorageReportList() {
    }

    public StorageReportList(List<String> storageReportList) {
        this.storageReportList = storageReportList;
        this.schemaVersion = SCHEMA_VERSION;
    }

    public List<String> getStorageReportList() {
        return storageReportList;
    }

    /*
     * Generated by IntelliJ
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StorageReportList that = (StorageReportList) o;

        if (storageReportList != null ? !storageReportList
            .equals(that.storageReportList) : that.storageReportList != null) {
            return false;
        }

        return true;
    }

    /*
     * Generated by IntelliJ
     */
    @Override
    public int hashCode() {
        return storageReportList != null ? storageReportList.hashCode() : 0;
    }
}
