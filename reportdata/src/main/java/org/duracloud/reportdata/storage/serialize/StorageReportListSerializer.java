/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.reportdata.storage.serialize;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportListSerializer {

    private XStream xstream;

    public StorageReportListSerializer() {
        xstream = new XStream(new DomDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.addDefaultImplementation(LinkedList.class, List.class);

        // Class names
        xstream.alias("storage-report-list", List.class);
        xstream.alias("report-id", String.class);
    }

    public String serializeReportList(List<String> storageReportList) {
        return xstream.toXML(storageReportList);
    }

    public List<String> deserializeReportList(String xml) {
        if(xml == null || xml.equals("")) {
            throw new RuntimeException("Report list cannot be null or empty");
        } else {
            return (List<String>)xstream.fromXML(xml);
        }
    }

    public List<String> deserializeReportList(InputStream stream) {
        if(stream == null) {
            throw new RuntimeException("Report stream cannot be null");
        } else {
            return (List<String>)xstream.fromXML(stream);
        }
    }
}
