/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

import java.io.File;

/**
 * Sets up the log facilities. Currently using Log4j code directly to do the
 * work in order to allow placing logs in the work dir and not include any
 * unnecessary log info on the command line.
 *
 * This setup also provides the potential of changing log level on the fly
 * without requiring additional system, env, or JVM variables to be set by
 * the user ((like the sync tool), but this is not currently implemented.
 *
 * Log4j code is contained to this class.
 *
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class LogUtil {

    private Logger retLogger;
    private Logger dcLogger;
    private Level logLevel;
    private String logLocation;

    public LogUtil() {
        retLogger = Logger.getLogger("org.duracloud.retrieval");
        dcLogger = Logger.getLogger("org.duracloud");
        logLevel = Level.DEBUG;
        setLogLevel(logLevel);
    }

    public void setupLogger(File backupDir) {
        // Clear root logger
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.removeAllAppenders();

        // Set up retrieval tool log
        String pattern = "%p %d{yyyy-MM-dd' 'HH:mm:ss.SSS} [%t] (%c{1}) %m%n";
        PatternLayout layout = new PatternLayout(pattern);
        File logDir = new File(backupDir, "logs");
        File logFile = new File(logDir, "retrieval-tool.log");
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
        retLogger.addAppender(fileAppender);

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
        rootLogger.setLevel(Level.INFO);
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
        retLogger.setLevel(level);
        dcLogger.setLevel(level);
    }

    public String getLogLevel() {
        return logLevel.toString();
    }
}
