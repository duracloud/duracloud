/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.binding;

import org.springframework.binding.convert.service.DefaultConversionService;

public class ConversionService
        extends DefaultConversionService {

    @Override
    protected void addDefaultConverters() {
        super.addDefaultConverters();
        addConverter(new StringToMultipartFile());
    }
}
