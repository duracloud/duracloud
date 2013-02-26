/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import com.thoughtworks.xstream.XStream;
import org.duracloud.sync.config.SyncToolConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

public class SyncToolConfigSerializer {

    /**
     * 
     * @param syncToolConfigXmlPath
     * @return deserialized config if the xml file is available and valid,
     *         otherwise null
     */
    public static SyncToolConfig deserialize(String syncToolConfigXmlPath)
        throws IOException {
        File f = new File(syncToolConfigXmlPath);
        InputStream fis = new FileInputStream(f);
        XStream xstream = new XStream();
        configure(xstream);
        SyncToolConfig c = (SyncToolConfig)xstream.fromXML(fis);
        fis.close();
        return c;
    }
    private static void configure(XStream xstream){
        xstream.alias("syncToolConfig", SyncToolConfig.class);
    }
    public static void serialize(SyncToolConfig syncToolConfig,
                                 String syncToolConfigXmlPath)
        throws IOException {
        XStream xstream = new XStream();
        configure(xstream);
        File file = new File(syncToolConfigXmlPath);
        file.getParentFile().mkdirs();
        
        Writer w = new FileWriter(file);
        xstream.toXML(syncToolConfig, w);
        w.close();
    }

}
