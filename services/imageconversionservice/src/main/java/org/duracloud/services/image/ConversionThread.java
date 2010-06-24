/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.duracloud.client.ContentStore;

import java.io.File;

/**
 * Thread in which the conversion manager does its work.
 *
 * @author Bill Branan
 *         Date: Jan 28, 2010
 */
public class ConversionThread extends Thread {

    private ConversionManager conversionManager;

    public ConversionThread(ContentStore contentStore,
                            File workDir,
                            String toFormat,
                            String colorSpace,
                            String sourceSpaceId,
                            String destSpaceId,
                            String namePrefix,
                            String nameSuffix,
                            int threads) {
        conversionManager = new ConversionManager(contentStore,
                                                  workDir,
                                                  toFormat,
                                                  colorSpace,
                                                  sourceSpaceId,
                                                  destSpaceId,
                                                  namePrefix,
                                                  nameSuffix,
                                                  threads);
    }

    public void run() {
        conversionManager.startConversion();
    }

    public String getConversionStatus() {
        return conversionManager.getConversionStatus();
    }

    public void stopConversion() {
        conversionManager.stopConversion();
    }
}
