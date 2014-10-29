/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.duracloud.common.util.EncryptionUtil;
import org.jdom.Element;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class DatabaseConfigXmlUtil {
    
    public static Element marshall(DatabaseConfig millDbConfig, String elementName) {
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        Element db = new Element(elementName);

        String host = millDbConfig.getHost();
        if (null != host) {
            db.addContent(new Element("host").setText(host));
        }

        int port = millDbConfig.getPort();
        db.addContent(new Element("port").setText(port + ""));

        String name = millDbConfig.getName();
        if (null != name) {
            db.addContent(new Element("name").setText(name));
        }

        String username = millDbConfig.getUsername();
        if (null != username) {
            db.addContent(new Element("username").setText(username));
        }

        String password = millDbConfig.getPassword();
        if (null != password) {
            db.addContent(new Element("password").setText(encryptionUtil.encrypt(password)));
        }
        return db;
    }
    
    public static DatabaseConfig unmarshalDatabaseConfig(Element dbConfigElement) {
        EncryptionUtil encryptionUtil = new EncryptionUtil();

        DatabaseConfig millDbConfig = new DatabaseConfig();

        String host = dbConfigElement.getChildText("host");
        if(null != host) {
            millDbConfig.setHost(host);
        }

        String port = dbConfigElement.getChildText("port");
        if(null != port) {
            millDbConfig.setPort(Integer.parseInt(port));
        }

        String name = dbConfigElement.getChildText("name");
        if(null != name) {
            millDbConfig.setName(name);
        }

        String username = dbConfigElement.getChildText("username");
        if(null != username) {
            millDbConfig.setUsername(username);
        }

        String password = dbConfigElement.getChildText("password");
        if(null != password) {
            millDbConfig.setPassword(encryptionUtil.decrypt(password));
        }
        return millDbConfig;
    }
}
