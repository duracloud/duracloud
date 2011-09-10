/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.appconfig.xml.DuraserviceInitDocumentBinding;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the configuration elements for duraservice.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class DuraserviceConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(DuraserviceConfig.class);

    private static final String INIT_RESOURCE = "/services";

    public static final String QUALIFIER = "duraservice";
    protected static final String primaryInstanceKey = "primary-instance";
    protected static final String userStoreKey = "user-storage";
    protected static final String serviceStoreKey = "service-storage";

    protected static final String hostKey = "host";
    protected static final String portKey = "port";
    protected static final String contextKey = "context";
    protected static final String usernameKey = "username";
    protected static final String passwordKey = "password";
    protected static final String servicesAdminPortKey = "services-admin-port";
    protected static final String servicesAdminContextKey = "services-admin-context";
    protected static final String msgBrokerUrlKey = "msg-broker-url";
    protected static final String spaceIdKey = "space-id";
    protected static final String serviceXmlIdKey = "service-xml-id";

    private PrimaryInstance primaryInstance = new PrimaryInstance();
    private UserStore userStore = new UserStore();
    private ServiceStore serviceStore = new ServiceStore();

    protected String getQualifier() {
        return QUALIFIER;
    }

    public String asXml() {
        return DuraserviceInitDocumentBinding.createDocumentFrom(this);
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        String prefix = getPrefix(key);
        String suffix = getSuffix(key);

        if (prefix.equalsIgnoreCase(primaryInstanceKey)) {
            loadPrimaryInstance(suffix, value);

        } else if (prefix.equalsIgnoreCase(userStoreKey)) {
            loadUserStore(suffix, value);

        } else if (prefix.equalsIgnoreCase(serviceStoreKey)) {
            loadServiceStore(suffix, value);

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadPrimaryInstance(String key, String value) {
        if (key.equalsIgnoreCase(hostKey)) {
            this.primaryInstance.setHost(value);

        } else if (key.equalsIgnoreCase(servicesAdminPortKey)) {
            this.primaryInstance.setServicesAdminPort(value);

        } else if (key.equalsIgnoreCase(servicesAdminContextKey)) {
            this.primaryInstance.setServicesAdminContext(value);

        } else {
            String msg = "unknown instance key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadUserStore(String key, String value) {
        if (key.equalsIgnoreCase(hostKey)) {
            this.userStore.setHost(value);

        } else if (key.equalsIgnoreCase(portKey)) {
            this.userStore.setPort(value);

        } else if (key.equalsIgnoreCase(contextKey)) {
            this.userStore.setContext(value);

        } else if (key.equalsIgnoreCase(msgBrokerUrlKey)) {
            this.userStore.setMsgBrokerUrl(value);

        } else {
            String msg = "unknown userStore key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadServiceStore(String key, String value) {
        if (key.equalsIgnoreCase(hostKey)) {
            this.serviceStore.setHost(value);

        } else if (key.equalsIgnoreCase(portKey)) {
            this.serviceStore.setPort(value);

        } else if (key.equalsIgnoreCase(contextKey)) {
            this.serviceStore.setContext(value);

        } else if (key.equalsIgnoreCase(usernameKey)) {
            this.serviceStore.setUsername(value);

        } else if (key.equalsIgnoreCase(passwordKey)) {
            this.serviceStore.setPassword(value);

        } else if (key.equalsIgnoreCase(spaceIdKey)) {
            this.serviceStore.setSpaceId(value);

        } else if (key.equalsIgnoreCase(serviceXmlIdKey)) {
            this.serviceStore.setServiceXmlId(value);

        } else {
            String msg = "unknown srvStore key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    public PrimaryInstance getPrimaryInstance() {
        return primaryInstance;
    }

    public void setPrimaryInstance(PrimaryInstance primaryInstance) {
        this.primaryInstance = primaryInstance;
    }

    public UserStore getUserStore() {
        return userStore;
    }

    public void setUserStore(UserStore userStore) {
        this.userStore = userStore;
    }
    
    public ServiceStore getServiceStore() {
        return serviceStore;
    }

    public void setServiceStore(ServiceStore serviceStore) {
        this.serviceStore = serviceStore;
    }

    public static class PrimaryInstance {
        private String host;
        private String servicesAdminPort;
        private String servicesAdminContext;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getServicesAdminPort() {
            return servicesAdminPort;
        }

        public void setServicesAdminPort(String servicesAdminPort) {
            this.servicesAdminPort = servicesAdminPort;
        }

        public String getServicesAdminContext() {
            return servicesAdminContext;
        }

        public void setServicesAdminContext(String servicesAdminContext) {
            this.servicesAdminContext = servicesAdminContext;
        }
    }

    public static class UserStore {
        private String host;
        private String port;
        private String context;
        private String msgBrokerUrl;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public String getMsgBrokerUrl() {
            return msgBrokerUrl;
        }

        public void setMsgBrokerUrl(String msgBrokerUrl) {
            this.msgBrokerUrl = msgBrokerUrl;
        }
    }

    public static class ServiceStore {
        private String host;
        private String port;
        private String context;
        private String username;
        private String password;
        private String spaceId;
        private String serviceXmlId;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSpaceId() {
            return spaceId;
        }

        public void setSpaceId(String spaceId) {
            this.spaceId = spaceId;
        }

        public String getServiceXmlId() {
            return serviceXmlId;
        }

        public void setServiceXmlId(String serviceXmlId) {
            this.serviceXmlId = serviceXmlId;
        }
    }

}
