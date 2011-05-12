/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.xml;

import org.duracloud.appconfig.domain.DurareportConfig;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DurareportInitDocumentBinding {

    private static final Logger log = LoggerFactory
        .getLogger(DurareportInitDocumentBinding.class);

    /**
     * This method deserializes the provided xml into a durareport config object.
     *
     * @param xml
     * @return
     */
    public static DurareportConfig createDurareportConfigFrom(InputStream xml) {
        DurareportConfig config = new DurareportConfig();

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
                "Durareport configuration xml: " + e.getMessage();
            log.error(error);
            throw new DuraCloudRuntimeException(error, e);
        }

        return config;
    }

    /**
     * This method serializes the provide durareport configuration into xml.
     *
     * @param durareportConfig
     * @return
     */
    public static String createDocumentFrom(DurareportConfig durareportConfig) {
        StringBuilder xml = new StringBuilder();

        if (null != durareportConfig) {
            String durastoreHost = durareportConfig.getDurastoreHost();
            String durastorePort = durareportConfig.getDurastorePort();
            String durastoreContext = durareportConfig.getDurastoreContext();
            String duraserviceHost = durareportConfig.getDuraserviceHost();
            String duraservicePort = durareportConfig.getDuraservicePort();
            String duraserviceContext = durareportConfig.getDuraserviceContext();

            xml.append("<durareportConfig>");
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
            xml.append("</durareportConfig>");
        }
        return xml.toString();
    }

}
