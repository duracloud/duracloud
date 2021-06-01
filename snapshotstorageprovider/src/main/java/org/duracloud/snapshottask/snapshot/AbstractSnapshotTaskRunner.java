/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.storage.provider.TaskRunner;

public abstract class AbstractSnapshotTaskRunner implements TaskRunner {

    private String bridgeAppHost;
    private String bridgeAppPort;
    private String bridgeAppUser;
    private String bridgeAppPass;

    public AbstractSnapshotTaskRunner(
        String bridgeAppHost, String bridgeAppPort, String bridgeAppUser,
        String bridgeAppPass) {
        this.bridgeAppHost = bridgeAppHost;
        this.bridgeAppPort = bridgeAppPort;
        this.bridgeAppUser = bridgeAppUser;
        this.bridgeAppPass = bridgeAppPass;
    }

    protected String getBridgeAppHost() {
        return bridgeAppHost;
    }

    protected String getBridgeAppPort() {
        return bridgeAppPort;
    }

    protected String getBridgeAppUser() {
        return bridgeAppUser;
    }

    protected String getBridgeAppPass() {
        return bridgeAppPass;
    }

    /*
     * Create URL to call bridge app
     */
    protected String buildBridgeBaseURL() {
        String protocol = "443".equals(bridgeAppPort) ? "https" : "http";
        return MessageFormat.format("{0}://{1}:{2}/bridge",
                                    protocol,
                                    bridgeAppHost,
                                    bridgeAppPort);
    }

    protected RestHttpHelper createRestHelper() {
        RestHttpHelper restHelper =
            new RestHttpHelper(new Credential(bridgeAppUser, bridgeAppPass));
        return restHelper;
    }

    /**
     * A helper method that takes a json string and extracts the value of the specified
     * property.
     *
     * @param json     the json string
     * @param propName the name of the property to extract
     * @param <T>      The type for the value expected to be returned.
     * @return the value of the specified property
     * @throws IOException
     */
    protected <T> T getValueFromJson(String json, String propName) throws IOException {
        return (T) jsonStringToMap(json).get(propName);
    }

    /**
     * A helper method that converts a json string into a map object.
     *
     * @param json the json string
     * @return a map representing the json string.
     * @throws IOException
     */
    protected Map jsonStringToMap(String json) throws IOException {
        return new JaxbJsonSerializer<HashMap>(HashMap.class).deserialize(json);
    }

    /**
     * A helper method that extracts the "message" property from the json string.
     *
     * @param json the json string
     * @return the value of the "message" field.
     * @throws IOException
     */
    protected String getMessageValue(String json) throws IOException {
        return getValueFromJson(json, "message");
    }

    /**
     * Pause execution
     *
     * @param seconds to wait
     */
    protected void wait(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (InterruptedException e) {
            // Exit sleep on interruption
        }
    }

}
