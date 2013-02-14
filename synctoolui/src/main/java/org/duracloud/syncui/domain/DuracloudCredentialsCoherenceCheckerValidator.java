/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.syncui.service.ContentStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class DuracloudCredentialsCoherenceCheckerValidator
    implements
    ConstraintValidator<DuracloudCredentialsCoherenceChecker, DuracloudCredentialsForm> {

    private static Logger log =
        LoggerFactory.getLogger(DuracloudCredentialsCoherenceCheckerValidator.class);

    private ContentStoreFactory contentStoreFactory;

    @Autowired
    public DuracloudCredentialsCoherenceCheckerValidator(
        ContentStoreFactory contentStoreFactory) {
        this.contentStoreFactory = contentStoreFactory;
    }

    @Override
    public void
        initialize(DuracloudCredentialsCoherenceChecker constraintAnnotation) {
    }

    @Override
    public boolean isValid(DuracloudCredentialsForm value,
                           ConstraintValidatorContext context) {
        try {
            ContentStore cs = contentStoreFactory.create(value);
            return cs != null;
        } catch (ContentStoreException ex) {
            log.warn("invalid credentials: " + ex.getMessage(), ex);
            return false;
        }
    }

}
