/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import java.io.Serializable;

import org.duracloud.syncui.service.RunMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This class backs the Mode form.
 * @author Daniel Bernstein
 *
 */
@Component("modeForm")
public class ModeForm implements Serializable {
    private Logger log = LoggerFactory.getLogger(ModeForm.class);
    private static final long serialVersionUID = 1L;

    private RunMode mode = RunMode.CONTINUOUS;

    public RunMode getMode() {
        return mode;
    }

    public void setMode(RunMode mode) {
        log.debug("setting mode {}", mode);
        this.mode = mode;
    }
}
