/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.util;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 * @author Andrew Woods
 */
public class ApplicationConfig {

    protected static final Logger log =
            LoggerFactory.getLogger(ApplicationConfig.class);

    protected static Properties getPropsFromResource(String resourceName) {
        Properties props = new Properties();
        AutoCloseInputStream in =
                new AutoCloseInputStream(ApplicationConfig.class
                        .getClassLoader().getResourceAsStream(resourceName));
        try {
            props.load(in);
        } catch (Exception e) {
            String error = "Unable to find resource: '" + resourceName + "': "
                    + e.getMessage();
            throw new RuntimeException(error, e);
        }
        return props;
    }

    public static Properties getPropsFromXml(String propsXml) {
        AutoCloseInputStream in =
                new AutoCloseInputStream(new ByteArrayInputStream(propsXml
                        .getBytes()));

        return getPropsFromXmlStream(in);
    }

    public static Properties getPropsFromXmlStream(InputStream propsXmlStream) {
        Properties props = new Properties();
        try {
            props.loadFromXML(propsXmlStream);
        } catch (InvalidPropertiesFormatException e) {
            log.error(e.getMessage());
            log.error(ExceptionUtil.getStackTraceAsString(e));
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(ExceptionUtil.getStackTraceAsString(e));
            throw new RuntimeException(e);
        } finally {
            if (propsXmlStream != null) {
                try {
                    propsXmlStream.close();
                } catch(IOException e) {
                    String error =
                        "Error closing properties stream: " + e.getMessage();
                    throw new RuntimeException(error, e);
                }
            }
        }

        return props;
    }

    public static Properties getPropsFromXmlResource(String resourceName) {
        Properties props = new Properties();
        AutoCloseInputStream in =
                new AutoCloseInputStream(ApplicationConfig.class
                        .getClassLoader().getResourceAsStream(resourceName));
        try {
            props.loadFromXML(in);
        } catch (Exception e) {
            String error = "Unable to find resource: '" + resourceName + "': "
                    + e.getMessage();
            throw new RuntimeException(error);
        }
        return props;
    }

    public static String getXmlFromProps(Properties props) {
        String comment = null;
        String xml = new String();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            props.storeToXML(os, comment);
            os.flush();
            xml = os.toString();
        } catch (IOException e) {
            String error = "IO exception for props: '" + props + "':" +
                e.getMessage();
            log.error(error);
            log.error(ExceptionUtil.getStackTraceAsString(e));
            throw new RuntimeException(error, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    String error =
                        "Error closing xml stream: " + e.getMessage();
                    throw new RuntimeException(error, e);
                }
            }
        }

        return xml;
    }

}
