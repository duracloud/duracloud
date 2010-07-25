/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.binding;

import org.springframework.binding.convert.converters.StringToObject;
import org.springframework.web.multipart.MultipartFile;

public class StringToMultipartFile
        extends StringToObject {

    public StringToMultipartFile() {
        super(MultipartFile.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Object toObject(String string, Class targetClass)
            throws Exception {
        return null;
    }

    @Override
    protected String toString(Object object) throws Exception {
        if (object != null) {
            MultipartFile file = (MultipartFile) object;
            return file.getOriginalFilename();
        }
        return null;
    }

}
