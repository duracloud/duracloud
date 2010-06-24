/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles the conversion of image files from one format to another.
 * Image files are pulled from the source space, converted locally, and the
 * result is stored in the destination space.
 *
 * @author Bill Branan
 *         Date: Jan 28, 2010
 */
public class ConversionManager {

    private final Logger log = LoggerFactory.getLogger(ConversionManager.class);

    private boolean conversionComplete = false;
    private boolean continueConversion = true;
    private Map<String, String> extMimeMap;

    private ContentStore contentStore;
    private File workDir;
    private String toFormat;
    private String colorSpace;
    private String sourceSpaceId;
    private String destSpaceId;
    private String namePrefix;
    private String nameSuffix;

    private String convertScript;
    private ThreadPoolExecutor workerPool;
    private ConversionResultProcessor resultProcessor;

    public ConversionManager(ContentStore contentStore,
                             File workDir,
                             String toFormat,
                             String colorSpace,
                             String sourceSpaceId,
                             String destSpaceId,
                             String namePrefix,
                             String nameSuffix,
                             int threads) {
        this.contentStore = contentStore;
        this.workDir = workDir;
        this.toFormat = toFormat;
        this.colorSpace = colorSpace;
        this.sourceSpaceId = sourceSpaceId;
        this.destSpaceId = destSpaceId;
        this.namePrefix = namePrefix;
        this.nameSuffix = nameSuffix;

        extMimeMap = new HashMap<String, String>();
        loadExtMimeMap();

        resultProcessor =
            new ConversionResultProcessor(contentStore, destSpaceId, workDir);

        workerPool =
            new ThreadPoolExecutor(threads,
                                   threads,
                                   Long.MAX_VALUE,
                                   TimeUnit.NANOSECONDS,
                                   new SynchronousQueue(),
                                   new ThreadPoolExecutor.AbortPolicy());        
    }

    private void loadExtMimeMap() {
        // Load supported file types: gif, jpg, png, tiff, jp2, bmp, pdf, psd
        extMimeMap.put("default", "application/octet-stream");
        extMimeMap.put("gif", "image/gif");
        extMimeMap.put("jpg", "image/jpeg");
        extMimeMap.put("png", "image/png");
        extMimeMap.put("tiff", "image/tiff");
        extMimeMap.put("jp2", "image/jp2");
        extMimeMap.put("bmp", "image/bmp");
        extMimeMap.put("pdf", "application/pdf");
        extMimeMap.put("psd", "image/psd");
    }

    public void startConversion() {
        printStartMessage();

        workDir.setWritable(true);
        checkDestSpace();
        Iterator<String> contentIds = getContentList();
        String convertScript = getConvertScript();

        while (continueConversion && contentIds.hasNext()) {
            String contentId = contentIds.next();

            // Perform conversion for files matching suffix
            if(fileMatchesSuffix(contentId)) {
                ConversionWorker worker =
                    new ConversionWorker(contentStore,
                                         sourceSpaceId,
                                         destSpaceId,
                                         contentId,
                                         workDir,
                                         toFormat,
                                         extMimeMap,
                                         convertScript,
                                         resultProcessor);
                boolean successStartingWorker = false;
                while(!successStartingWorker) {
                    try {
                        workerPool.execute(worker);
                        successStartingWorker = true;
                    } catch(RejectedExecutionException e) {
                        successStartingWorker = false;
                        sleep(10000);
                    }
                }
            }
        }

        workerPool.shutdown();
        try {
            workerPool.awaitTermination(60, TimeUnit.MINUTES);
        } catch(InterruptedException e) {
            log.warn("Interruped waiting for worker pool to shut down. " +
                     "Assuming shutdown is complete.");
        }

        // Indicate that the conversion is complete
        conversionComplete = true;

        printEndMessage();
    }

    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.warn("ChangeWatcher thread interrupted");
        }
    }

    private void printStartMessage() {
        StringBuffer startMsg = new StringBuffer();
        startMsg.append("Starting Image Conversion. Image source space: ");
        startMsg.append(sourceSpaceId);
        startMsg.append(". Image destination space: ");
        startMsg.append(destSpaceId);
        startMsg.append(". Converting to format: '");
        startMsg.append(toFormat);
        startMsg.append("'. Name prefix: '");
        startMsg.append(namePrefix);
        startMsg.append("'. Name suffix: '");
        startMsg.append(nameSuffix);
        startMsg.append("'.");

        log.info(startMsg.toString());
    }

    private void printEndMessage() {
        log.info(getConversionStatus());
    }

    protected String getConversionStatus() {
        return resultProcessor.getConversionStatus(conversionComplete);
    }

    /*
     * Determins if a file includes the user provided suffix
     */
    private boolean fileMatchesSuffix(String contentId) {
        boolean fileMatchSuffix = false;
        if (nameSuffix != null && !nameSuffix.equals("")) {
            if (contentId.endsWith(nameSuffix)) {
                fileMatchSuffix = true;
            }
        } else {
            fileMatchSuffix = true;
        }
        return fileMatchSuffix;
    }

    /*
     * Get content list from space where source images reside (limit to prefix)
     */
    private Iterator<String> getContentList() {
        Iterator<String> contentIds;
        try {
            contentIds =
                contentStore.getSpaceContents(sourceSpaceId, namePrefix);
        } catch(ContentStoreException e) {
            throw new RuntimeException("Conversion could not be started due" +
                                       " to error: " + e.getMessage(), e);
        }
        return contentIds;
    }

    /*
     * Ensure that the destination space exists
     */
    private void checkDestSpace() {
        try {
            try {
                contentStore.getSpaceMetadata(destSpaceId);
            } catch (NotFoundException e) {
                contentStore.createSpace(destSpaceId, null);
            }
        } catch(ContentStoreException e) {
            String err = "Could not access destination space " + destSpaceId +
                         "due to error: " + e.getMessage();
            throw new RuntimeException(err, e);
        }
    }

    /*
     * Gets the script used to perform conversions
     */
    private String getConvertScript() {
        try {
            if(convertScript == null) {
                convertScript = createScript();
            }
        } catch(IOException e) {
            String err = "Could not create conversion script due to error: " +
                         e.getMessage();
            throw new RuntimeException(err, e);
        }
        return convertScript;
    }

    /*
     * Creates the script used to perform conversions
     */
    private String createScript() throws IOException {
        String fileName;
        List<String> scriptLines = new ArrayList<String>();

        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().indexOf("windows") >= 0) { // windows
            fileName = "convert.bat";
            if(includeColorspace()) {
                scriptLines.add("mogrify -profile sRGB.icm %2");
            }
            scriptLines.add("mogrify -format %1 %2");
        } else { // linux
            fileName = "convert.sh";
            scriptLines.add("#!/bin/bash");
            if(includeColorspace()) {
                scriptLines.add("sudo mogrify -profile sRGB.icm $2");
            }
            scriptLines.add("sudo mogrify -format $1 $2");
        }

        File scriptFile = new File(workDir, fileName);
        FileUtils.writeLines(scriptFile, scriptLines);
        scriptFile.setExecutable(true);
        return scriptFile.getAbsolutePath();
    }

    private boolean includeColorspace() {
        if(colorSpace != null &&
           colorSpace.length() > 0 &&
           colorSpace.equals("sRGB")) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Indicate that conversion should stop after the files currently being
     * processed have completed
     */
    public void stopConversion() {
        continueConversion = false;
        workerPool.shutdown();
    }
}
