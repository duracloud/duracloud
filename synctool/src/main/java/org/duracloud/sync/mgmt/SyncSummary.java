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

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * A class that describes a completed sync operation.
 * 
 * @author Daniel Bernstein
 * 
 */
public class SyncSummary {
    public static enum Status {
        SUCCESS, FAILURE
    }

    private File file;
    private Date start;
    private Date stop;
    private Status status;
    private String message;
    
    public SyncSummary(
        File file, Date start, Date stop, Status status, String message) {
        super();
        this.file = file;
        this.start = start;
        this.stop = stop;
        this.status = status;
        this.message = message;
    }

    public String getDurationAsString(){
        long duration = this.stop.getTime()-this.start.getTime();
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
    
    public File getFile() {
        return file;
    }

    public Date getStart() {
        return start;
    }

    public Date getStop() {
        return stop;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

}
