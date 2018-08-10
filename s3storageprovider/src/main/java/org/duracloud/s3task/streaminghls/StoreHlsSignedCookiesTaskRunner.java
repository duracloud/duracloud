/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import org.duracloud.StorageTaskConstants;
import org.duracloud.common.data.StringDataStore;
import org.duracloud.s3storageprovider.dto.StoreSignedCookieTaskParameters;
import org.duracloud.s3storageprovider.dto.StoreSignedCookieTaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores a set of signed cookies for retrieval through a CloudFront origin call
 *
 * @author: Bill Branan
 * Date: Aug 10, 2018
 */
public class StoreHlsSignedCookiesTaskRunner extends BaseHlsTaskRunner {

    private final Logger log = LoggerFactory.getLogger(StoreHlsSignedCookiesTaskRunner.class);

    private static final String TASK_NAME = StorageTaskConstants.STORE_SIGNED_COOKIES_TASK_NAME;

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        // Validate parameters are syntactically correct
        StoreSignedCookieTaskParameters.deserialize(taskParameters);

        log.info("Performing " + TASK_NAME + " task");

        String token = StringDataStore.getInstance().storeData(taskParameters);

        StoreSignedCookieTaskResult taskResult = new StoreSignedCookieTaskResult();
        taskResult.setToken(token);

        String toReturn = taskResult.serialize();
        log.info("Result of " + TASK_NAME + " task: " + toReturn);
        return toReturn;
    }

}