/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.xml;

import org.duracloud.appconfig.domain.DuradminConfig;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * This class (de)serializes duradmin configuration between objects and xml.
 *
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DuradminInitDocumentBinding {

    private static final Logger log = LoggerFactory.getLogger(
        DuraserviceInitDocumentBinding.class);

    /**
     * This method deserializes the provided xml into a duradmin config object.
     *
     * @param xml
     * @return
     */
    public static DuradminConfig createDuradminConfigFrom(InputStream xml) {
        DuradminConfig config = new DuradminConfig();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element root = doc.getRootElement();

            config.setDurastoreHost(root.getChildText("durastoreHost"));
            config.setDurastorePort(root.getChildText("durastorePort"));
            config.setDurastoreContext(root.getChildText("durastoreContext"));
            config.setDuraserviceHost(root.getChildText("duraserviceHost"));
            config.setDuraservicePort(root.getChildText("duraservicePort"));
            config.setDuraserviceContext(root.getChildText("duraserviceContext"));

        } catch (Exception e) {
            String error = "Error encountered attempting to parse " +
                "Duradmin configuration xml: " + e.getMessage();
            log.error(error);
            throw new DuraCloudRuntimeException(error, e);
        }

        return config;
    }

    /**
     * This method serializes the provide duradmin configuration into xml.
     *
     * @param duradminConfig
     * @return
     */
    public static String createDocumentFrom(DuradminConfig duradminConfig) {
        StringBuilder xml = new StringBuilder();

        if (null != duradminConfig) {
            String durastoreHost = duradminConfig.getDurastoreHost();
            String durastorePort = duradminConfig.getDurastorePort();
            String durastoreContext = duradminConfig.getDurastoreContext();
            String duraserviceHost = duradminConfig.getDuraserviceHost();
            String duraservicePort = duradminConfig.getDuraservicePort();
            String duraserviceContext = duradminConfig.getDuraserviceContext();

            xml.append("<duradminConfig>");
            xml.append("  <durastoreHost>" + durastoreHost);
            xml.append("</durastoreHost>");
            xml.append("  <durastorePort>" + durastorePort);
            xml.append("</durastorePort>");
            xml.append("  <durastoreContext>" + durastoreContext);
            xml.append("</durastoreContext>");
            xml.append("  <duraserviceHost>" + duraserviceHost);
            xml.append("</duraserviceHost>");
            xml.append("  <duraservicePort>" + duraservicePort);
            xml.append("</duraservicePort>");
            xml.append("  <duraserviceContext>" + duraserviceContext);
            xml.append("</duraserviceContext>");
            xml.append("</duradminConfig>");
        }
        return xml.toString();
    }

}
