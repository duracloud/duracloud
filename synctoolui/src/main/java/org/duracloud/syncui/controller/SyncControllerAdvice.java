/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adds a global exception handler for all controllers.
 * @author Daniel Bernstein
 *
 */
@ControllerAdvice
public class SyncControllerAdvice {
    @ExceptionHandler(Throwable.class)
    public ModelAndView handleException(Throwable t){
        ModelAndView mav = new ModelAndView("exception");
        t.fillInStackTrace();
        mav.addObject("message", t.getMessage());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter write = new PrintWriter(os);
        t.printStackTrace(write);
        write.close();
        
        mav.addObject("stackTrace", new String(os.toByteArray()));
        return mav;
    }
}
