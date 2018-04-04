/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.LineIterator;
import org.duracloud.syncui.service.SyncConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * A spring controller for log viewing and navigation
 *
 * @author Daniel Bernstein
 */
@Controller
public class LogController {
    private SyncConfigurationManager syncConfigurationManager;
    private static Logger log = LoggerFactory.getLogger(LogController.class);

    @Autowired
    public LogController(SyncConfigurationManager syncConfigurationManager) {
        this.syncConfigurationManager = syncConfigurationManager;
    }

    @RequestMapping(value = {"/log"})
    public String get() {
        log.debug("accessing log page");
        return "log";
    }

    @RequestMapping(value = {"/log"}, params = "download")
    public String download(HttpServletResponse response) throws IOException {
        log.debug("accessing log download page");
        StringBuffer contentDisposition = new StringBuffer();
        contentDisposition.append("attachment;filename=\"history.log\"");
        response.setHeader("Content-Disposition", contentDisposition.toString());
        File file =
            new File(syncConfigurationManager
                         .getWorkDirectory()
                         .getAbsoluteFile() + "/logs/history.log");

        PrintWriter writer = response.getWriter();
        if (file.exists()) {
            LineIterator it = new LineIterator(new FileReader(file));
            while (it.hasNext()) {
                writer.write(it.nextLine() + "\n");
            }
        } else {
            writer.write("The history log is empty.");
        }

        return null;

    }

}
