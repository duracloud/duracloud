/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.image;

import java.util.Date;

/**
 * @author: Bill Branan
 * Date: Apr 22, 2010
 */
public class ConversionResult {

    private Date conversionDate;
    private String sourceSpaceId;
    private String destSpaceId;
    private String contentId;
    private boolean success;
    private String errMessage;
    private long conversionTime;
    private long totalTime;
    private long fileSize;

    public ConversionResult(Date conversionDate,
                            String sourceSpaceId,
                            String destSpaceId,
                            String contentId,
                            boolean success,
                            String errMessage,
                            long conversionTime,
                            long totalTime,
                            long fileSize) {
        this.conversionDate = conversionDate;
        this.sourceSpaceId = sourceSpaceId;
        this.destSpaceId = destSpaceId;
        this.contentId = contentId;
        this.success = success;
        this.errMessage = errMessage;
        this.conversionTime = conversionTime;
        this.totalTime = totalTime;
        this.fileSize = fileSize;
    }

    public Date getConversionDate() {
        return conversionDate;
    }

    public void setConversionDate(Date conversionDate) {
        this.conversionDate = conversionDate;
    }

    public String getSourceSpaceId() {
        return sourceSpaceId;
    }

    public String getDestSpaceId() {
        return destSpaceId;
    }

    public String getContentId() {
        return contentId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public long getConversionTime() {
        return conversionTime;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public long getFileSize() {
        return fileSize;
    }
}
