/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.config;

import org.springframework.format.support.DefaultFormattingConversionService;
/**
 * 
 * @author Daniel Bernstein
 *
 */
public class ApplicationFormattingConversionService
    extends DefaultFormattingConversionService {

    public ApplicationFormattingConversionService(){
        super(true);
        addConverter(new StringTrimmerConverter());
    }
}
