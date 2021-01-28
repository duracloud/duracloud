/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud;

/**
 * @author: Bill Branan
 * Date: 3/6/2015
 */
public class StorageTaskConstants {

    private StorageTaskConstants() {
        // Ensures no instances are made of this class, as there are only static members.
    }

    //Base
    public static final String TASK_BASE_PATH = "/task";

    // HLS Streaming
    public static final String ENABLE_HLS_TASK_NAME = "enable-hls";
    public static final String DISABLE_HLS_TASK_NAME = "disable-hls";
    public static final String DELETE_HLS_TASK_NAME = "delete-hls";
    public static final String GET_HLS_URL_TASK_NAME = "get-url-hls";
    public static final String GET_SIGNED_COOKIES_URL_TASK_NAME = "get-signed-cookies-url";

    // Storage policy
    public static final String SET_STORAGE_POLICY_TASK_NAME = "set-storage-policy";

}
