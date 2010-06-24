/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.error;

import org.duracloud.client.error.ServicesException;

import java.util.List;

/**
 * On validation failure, this class propagates user friendly validation messages
 * that can be associated with a particular property.  In most cases, the "property"
 * should be the "name" of the offending UserConfig or ServiceConfig object. If the 
 * validation error is general - ie not associated with a single property, you may 
 * construct a ValidationError with only the error text.  In this case, the error 
 * will be displayed to the user at the top of the page.
 * 
 *
 * @author Danny Bernstein
 * @version $Id$
 */
public class InvalidServiceConfigurationException extends ServicesException {
    private static final long serialVersionUID = 104604226824986975L;

    private List<ValidationError> errors;
    public InvalidServiceConfigurationException(List<ValidationError> errors) {
        super("The service configuration is invalid.");
        this.errors = errors;
    }

    public List<ValidationError> getErrors(){
        return this.errors;
    }
    
    public static class ValidationError {
        private String propertyName;
        private String errorText;
        
        public ValidationError(String errorText){
            this(null, errorText);
        }

        public ValidationError(String fieldKey, String errorText) {
            super();
            this.propertyName = fieldKey;
            this.errorText = errorText;
        }

        public String getPropertyName() {
            return propertyName;
        }
        
        public String getErrorText() {
            return errorText;
        }
        
    }
}
