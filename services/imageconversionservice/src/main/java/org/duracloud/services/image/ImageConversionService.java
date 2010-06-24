/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.services.BaseService;
import org.duracloud.services.ComputeService;
import org.duracloud.common.model.Credential;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Dictionary;
import java.util.Map;

/**
 * Service which converts image files from one format to another
 *
 * @author Bill Branan
 *         Date: Jan 27, 2010
 */
public class ImageConversionService extends BaseService implements ComputeService, ManagedService {

    private final Logger log = LoggerFactory.getLogger(ImageConversionService.class);

    private static final String DEFAULT_DURASTORE_HOST = "localhost";
    private static final String DEFAULT_DURASTORE_PORT = "8080";
    private static final String DEFAULT_DURASTORE_CONTEXT = "durastore";
    private static final String DEFAULT_TO_FORMAT = "jp2";
    private static final String DEFAULT_COLORSPACE = "source";
    private static final String DEFAULT_SOURCE_SPACE_ID = "image-conversion-source";
    private static final String DEFAULT_DEST_SPACE_ID = "image-conversion-dest";
    private static final String DEFAULT_NAME_PREFIX = "";
    private static final String DEFAULT_NAME_SUFFIX = "";

    private ConversionThread conversionThread;

    private String duraStoreHost;
    private String duraStorePort;
    private String duraStoreContext;
    private String username;
    private String password;
    private String toFormat;
    private String colorSpace;
    private String sourceSpaceId;
    private String destSpaceId;
    private String namePrefix;
    private String nameSuffix;
    private int threads;

    @Override
    public void start() throws Exception {
        log.info("Starting Image Conversion Service as " + username +
                           ", with " + threads + " worker threads");
        this.setServiceStatus(ServiceStatus.STARTING);
        
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(duraStoreHost,
                                        duraStorePort,
                                        duraStoreContext);
        storeManager.login(new Credential(username, password));
        ContentStore contentStore = storeManager.getPrimaryContentStore();

        File workDir = new File(getServiceWorkDir());
        conversionThread = new ConversionThread(contentStore,
                                                workDir,
                                                toFormat,
                                                colorSpace,
                                                sourceSpaceId,
                                                destSpaceId,
                                                namePrefix,
                                                nameSuffix,
                                                threads);
        conversionThread.start();

        this.setServiceStatus(ServiceStatus.STARTED);        
    }

    @Override
    public void stop() throws Exception {
        log.info("Stopping Image Conversion Service");
        this.setServiceStatus(ServiceStatus.STOPPING);
        if(conversionThread != null) {
            conversionThread.stopConversion();
        }
        this.setServiceStatus(ServiceStatus.STOPPED);
    }

    @Override
    public Map<String, String> getServiceProps() {
        Map<String, String> props = super.getServiceProps();
        if(conversionThread != null) {
            props.put("conversionStatus",
                      conversionThread.getConversionStatus());
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    public void updated(Dictionary config) throws ConfigurationException {
        log("Attempt made to update Image Conversion Service configuration " +
            "via updated method. Updates should occur via class setters.");
    }

    public String getDuraStoreHost() {
        return duraStoreHost;
    }

    public void setDuraStoreHost(String duraStoreHost) {
        if(duraStoreHost != null && !duraStoreHost.equals("") ) {
            this.duraStoreHost = duraStoreHost;
        } else {
            log("Attempt made to set duraStoreHost to " + duraStoreHost +
                ", which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_HOST);
            this.duraStoreHost = DEFAULT_DURASTORE_HOST;
        }
    }

    public String getDuraStorePort() {
        return duraStorePort;
    }

    public void setDuraStorePort(String duraStorePort) {
        if(duraStorePort != null) {
            this.duraStorePort = duraStorePort;
        } else {
            log("Attempt made to set duraStorePort to null, which is not " +
                "valid. Setting value to default: " + DEFAULT_DURASTORE_PORT);
            this.duraStorePort = DEFAULT_DURASTORE_PORT;
        }
    }

    public String getDuraStoreContext() {
        return duraStoreContext;
    }

    public void setDuraStoreContext(String duraStoreContext) {
        if(duraStoreContext != null && !duraStoreContext.equals("")) {
            this.duraStoreContext = duraStoreContext;
        } else {
            log("Attempt made to set duraStoreContext to null or empty, " +
                "which is not valid. Setting value to default: " +
                DEFAULT_DURASTORE_CONTEXT);
            this.duraStoreContext = DEFAULT_DURASTORE_CONTEXT;
        }
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

    public String getToFormat() {
        return toFormat;
    }

    public void setToFormat(String toFormat) {
        if(toFormat != null && !toFormat.equals("")) {
            this.toFormat = toFormat;
        } else {
            log("Attempt made to set toFormat to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_TO_FORMAT);
            this.toFormat = DEFAULT_TO_FORMAT;
        }
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public void setColorSpace(String colorSpace) {
        if(colorSpace != null && !colorSpace.equals("")) {
            this.colorSpace = colorSpace;
        } else {
            log("Attempt made to set colorSpace to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_COLORSPACE);
            this.colorSpace = DEFAULT_COLORSPACE;
        }
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public void setSourceSpaceId(String sourceSpaceId) {
        if(sourceSpaceId != null && !sourceSpaceId.equals("")) {
            this.sourceSpaceId = sourceSpaceId;
        } else {
            log("Attempt made to set sourceSpaceId to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_SOURCE_SPACE_ID);
            this.sourceSpaceId = DEFAULT_SOURCE_SPACE_ID;
        }
    }

    public String getDestSpaceId() {
        return destSpaceId;
    }

    public void setDestSpaceId(String destSpaceId) {
        if(destSpaceId != null && !destSpaceId.equals("")) {
            this.destSpaceId = destSpaceId;
        } else {
            log("Attempt made to set destSpaceId to to null or empty, " +
                ", which is not valid. Setting value to default: " +
                DEFAULT_DEST_SPACE_ID);
            this.destSpaceId = DEFAULT_DEST_SPACE_ID;
        }
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        if(namePrefix != null) {
            this.namePrefix = namePrefix;
        } else {
            log("Attempt made to set namePrefix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_PREFIX);
            this.namePrefix = DEFAULT_NAME_PREFIX;
        }
    }

    public String getNameSuffix() {
        return nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        if(nameSuffix != null) {
            this.nameSuffix = nameSuffix;
        } else {
            log("Attempt made to set nameSuffix to null, which is not valid. " +
                "Setting value to default: " + DEFAULT_NAME_SUFFIX);
            this.nameSuffix = DEFAULT_NAME_SUFFIX;
        }
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    private void log(String logMsg) {
        log.warn(logMsg);
    }
}
