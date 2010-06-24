/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.xml;

import org.duracloud.appconfig.domain.DuraserviceConfig;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * This class (de)serializes duraservice configuration between objects and xml.
 *
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DuraserviceInitDocumentBinding {
    private static final Logger log = LoggerFactory.getLogger(
        DuraserviceInitDocumentBinding.class);

    /**
     * This method deserializes the provided xml into a duraservice config object.
     *
     * @param xml
     * @return
     */
    public static DuraserviceConfig createStorageAccountsFrom(InputStream xml) {
        DuraserviceConfig config = new DuraserviceConfig();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element servicesConfig = doc.getRootElement();

            Element primaryInstance = servicesConfig.getChild(
                "primaryServiceInstance");
            DuraserviceConfig.PrimaryInstance instance = new DuraserviceConfig.PrimaryInstance();
            instance.setHost(primaryInstance.getChildText("host"));
            instance.setServicesAdminPort(primaryInstance.getChildText(
                "servicesAdminPort"));
            instance.setServicesAdminContext(primaryInstance.getChildText(
                "servicesAdminContext"));

            Element userStorage = servicesConfig.getChild("userStorage");
            DuraserviceConfig.UserStore userStore = new DuraserviceConfig.UserStore();
            userStore.setHost(userStorage.getChildText("host"));
            userStore.setPort(userStorage.getChildText("port"));
            userStore.setContext(userStorage.getChildText("context"));
            userStore.setMsgBrokerUrl(userStorage.getChildText("msgBrokerUrl"));

            Element serviceStorage = servicesConfig.getChild("serviceStorage");
            DuraserviceConfig.ServiceStore serviceStore = new DuraserviceConfig.ServiceStore();
            serviceStore.setHost(serviceStorage.getChildText("host"));
            serviceStore.setPort(serviceStorage.getChildText("port"));
            serviceStore.setContext(serviceStorage.getChildText("context"));
            serviceStore.setSpaceId(serviceStorage.getChildText("spaceId"));

            Element serviceComputeProvider = servicesConfig.getChild(
                "serviceCompute");
            DuraserviceConfig.ServiceCompute serviceCompute = new DuraserviceConfig.ServiceCompute();
            String computeProviderType = serviceComputeProvider.getChildText(
                "type");
            serviceCompute.setType(computeProviderType);
            serviceCompute.setImageId(serviceComputeProvider.getChildText(
                "imageId"));
            Element computeCredential = serviceComputeProvider.getChild(
                "computeProviderCredential");
            serviceCompute.setUsername(computeCredential.getChildText("username"));
            serviceCompute.setPassword(computeCredential.getChildText("password"));

        } catch (Exception e) {
            String error = "Error encountered attempting to parse " +
                "DuraService configuration xml: " + e.getMessage();
            log.error(error);
            throw new DuraCloudRuntimeException(error, e);
        }

        return config;
    }

    /**
     * This method serializes the provide duraservice configuration into xml.
     *
     * @param duraserviceConfig
     * @return
     */
    public static String createDocumentFrom(DuraserviceConfig duraserviceConfig) {
        StringBuilder xml = new StringBuilder();

        if (null != duraserviceConfig) {
            DuraserviceConfig.PrimaryInstance instance = duraserviceConfig.getPrimaryInstance();
            DuraserviceConfig.UserStore userStore = duraserviceConfig.getUserStore();
            DuraserviceConfig.ServiceStore serviceStore = duraserviceConfig.getServiceStore();
            DuraserviceConfig.ServiceCompute serviceCompute = duraserviceConfig.getServiceCompute();

            xml.append("<servicesConfig>");
            xml.append("  <primaryServiceInstance>");
            xml.append("    <host>" + instance.getHost() + "</host>");
            xml.append("    <servicesAdminPort>");
            xml.append(instance.getServicesAdminPort());
            xml.append("</servicesAdminPort>");
            xml.append("    <servicesAdminContext>");
            xml.append(instance.getServicesAdminContext());
            xml.append("</servicesAdminContext>");
            xml.append("  </primaryServiceInstance>");
            xml.append("  <userStorage>");
            xml.append("    <host>" + userStore.getHost() + "</host>");
            xml.append("    <port>" + userStore.getPort() + "</port>");
            xml.append("    <context>" + userStore.getContext() + "</context>");
            xml.append("    <msgBrokerUrl>" + userStore.getMsgBrokerUrl());
            xml.append("</msgBrokerUrl>");
            xml.append("  </userStorage>");
            xml.append("  <serviceStorage>");
            xml.append("    <host>" + serviceStore.getHost() + "</host>");
            xml.append("    <port>" + serviceStore.getPort() + "</port>");
            xml.append("    <context>" + serviceStore.getContext());
            xml.append("</context>");
            xml.append("    <spaceId>" + serviceStore.getSpaceId());
            xml.append("</spaceId>");
            xml.append("  </serviceStorage>");
            xml.append("  <serviceCompute>");
            xml.append("    <type>" + serviceCompute.getType() + "</type>");
            xml.append("    <imageId>" + serviceCompute.getImageId());
            xml.append("</imageId>");
            xml.append("    <computeProviderCredential>");
            xml.append("      <username>" + serviceCompute.getUsername());
            xml.append("</username>");
            xml.append("      <password>" + serviceCompute.getPassword());
            xml.append("</password>");
            xml.append("    </computeProviderCredential>");
            xml.append("  </serviceCompute>");
            xml.append("</servicesConfig>");
        }
        return xml.toString();
    }

}