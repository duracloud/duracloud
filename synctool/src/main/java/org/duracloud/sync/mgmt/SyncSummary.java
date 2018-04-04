/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import java.io.File;
import java.util.Date;

import org.duracloud.sync.endpoint.SyncResultType;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * A class that describes a completed sync operation.
 *
 * @author Daniel Bernstein
 */
public class SyncSummary {
    public static enum Status {
        SUCCESS, FAILURE
    }

    private String filename;
    private String absolutePath;
    private long length;
    private Date start;
    private Date stop;
    private String message;
    private SyncResultType type;

    public SyncSummary(File file,
                       Date start,
                       Date stop,
                       SyncResultType type,
                       String message) {
        super();
        this.filename = file.getName();
        this.absolutePath = file.getAbsolutePath();
        this.length = file.length();
        this.type = type;
        this.start = start;
        this.stop = stop;
        this.message = message;
    }

    public String getDurationAsString() {
        long duration = this.stop.getTime() - this.start.getTime();
        PeriodFormatter daysHoursMinutes = new PeriodFormatterBuilder()
            .appendDays()
            .appendSuffix(" day", " days")
            .appendSeparator(" ")
            .appendMinutes()
            .appendSuffix(" min", " min")
            .appendSeparator(" ")
            .appendSeconds()
            .appendSuffix(" sec", " secs")
            .toFormatter();

        Period period = new Period(duration);

        return daysHoursMinutes.print(period);
    }

    public Date getStart() {
        return start;
    }

    public Date getStop() {
        return stop;
    }

    public String getMessage() {
        return message;
    }

    public String getFilename() {
        return filename;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public long getLength() {
        return length;
    }

    public SyncResultType getType() {
        return type;
    }

}
