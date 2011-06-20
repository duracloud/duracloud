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
import org.duracloud.reportdata.storage.StorageReportInfo;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 6/2/11
 */
public class StorageReportInfoSerializer {

    private XStream xstream;

    public StorageReportInfoSerializer() {
        xstream = new XStream(new DomDriver());
        xstream.setMode(XStream.NO_REFERENCES);

        // Class names
        xstream.alias("storage-report-info", StorageReportInfo.class);

        // Fields
        xstream.aliasField("start-time",
                           StorageReportInfo.class,
                           "startTime");
        xstream.aliasField("current-count",
                           StorageReportInfo.class,
                           "currentCount");
        xstream.aliasField("final-count",
                           StorageReportInfo.class,
                           "finalCount");
        xstream.aliasField("completion-time",
                           StorageReportInfo.class,
                           "completionTime");
        xstream.aliasField("estimated-completion-time",
                           StorageReportInfo.class,
                           "estimatedCompletionTime");
        xstream.aliasField("next-scheduled-start-time",
                           StorageReportInfo.class,
                           "nextScheduledStartTime");
    }

    public String serializeReportInfo(StorageReportInfo storageReportInfo) {
        return xstream.toXML(storageReportInfo);
    }

    public StorageReportInfo deserializeReportInfo(String xml) {
        if(xml == null || xml.equals("")) {
            throw new RuntimeException("Report Info XML cannot be null or empty");
        } else {
            return (StorageReportInfo)xstream.fromXML(xml);
        }
    }

    public StorageReportInfo deserializeReport(InputStream stream) {
        if(stream == null) {
            throw new RuntimeException("Report Info stream cannot be null");
        } else {
            return (StorageReportInfo)xstream.fromXML(stream);
        }
    }

}
