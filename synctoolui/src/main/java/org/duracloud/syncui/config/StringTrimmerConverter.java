/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.config;

import org.springframework.core.convert.converter.Converter;

public class StringTrimmerConverter implements Converter<String,String> {

    @Override
    public String convert(String source) {
        if(source == null){
            return null;
        }
        
        String trimmed = ((String)source).trim();
        if(trimmed.length() == 0){
            return null;
        }else{
            return trimmed;
        }
    }
}
