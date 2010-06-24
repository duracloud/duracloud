/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;

/**
 * Sets up the log facilities. Currently using Log4j code directly to do the
 * work in order to allow placing logs in the backup dir and changing level
 * on the fly without requiring additional system, env, or JVM variables to
 * be set by the user. Log4j code is contained to this class.
 *
 * @author: Bill Branan
 * Date: Apr 6, 2010
 */
public class LogUtil {

    private Logger syncLogger;
    private Logger dcLogger;
    private Level logLevel;
    private String logLocation;

    public LogUtil() {
        syncLogger = Logger.getLogger("org.duracloud.sync");
        dcLogger = Logger.getLogger("org.duracloud");
        logLevel = Level.DEBUG;
        setLogLevel(logLevel);
    }

    public void setupLogger(File backupDir) {
        // Clear root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();

        // Set up sync tool log
        String pattern = "%p %d{yyyy-MM-dd' 'HH:mm:ss.SSS} [%t] (%c{1}) %m%n";
        PatternLayout layout = new PatternLayout(pattern);
        File logDir = new File(backupDir, "logs");
        File logFile = new File(logDir, "sync-tool.log");
        RollingFileAppender fileAppender;
        try {
            fileAppender = new RollingFileAppender(layout,
                                                   logFile.getAbsolutePath(),
                                                   true);
            fileAppender.setMaxFileSize("20MB");
            fileAppender.setMaxBackupIndex(5);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        syncLogger.addAppender(fileAppender);

        logLocation = logDir.getAbsolutePath();

        // Set up log to capture all duracloud logs
        File dcLogFile = new File(logDir, "duracloud.log");
        RollingFileAppender dcFileAppender;
        try {
            dcFileAppender = new RollingFileAppender(layout,
                                                     dcLogFile.getAbsolutePath(),
                                                     true);
            dcFileAppender.setMaxFileSize("20MB");
            dcFileAppender.setMaxBackupIndex(5);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        dcLogger.addAppender(dcFileAppender);

        // Set up log to capture all logs
        File depLogFile = new File(logDir, "complete.log");
        RollingFileAppender depFileAppender;
        try {
            depFileAppender = new RollingFileAppender(layout,
                                                      depLogFile.getAbsolutePath(),
                                                      true);
            depFileAppender.setMaxFileSize("20MB");
            depFileAppender.setMaxBackupIndex(5);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        rootLogger.addAppender(depFileAppender);
    }

    public String getLogLocation() {
        return logLocation;
    }

    public void setLogLevel(String level) {
        logLevel = Level.toLevel(level);
        setLogLevel(logLevel);
    }

    private void setLogLevel(Level level) {
        syncLogger.setLevel(level);
        dcLogger.setLevel(level);
    }

    public String getLogLevel() {
        return logLevel.toString();
    }
}
