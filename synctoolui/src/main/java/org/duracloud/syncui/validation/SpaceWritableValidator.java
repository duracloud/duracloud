/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.duracloud.client.ContentStore;
import org.duracloud.error.NotFoundException;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.ContentStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Test to determine if a user has write permissions for a given space.
 *
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 */

public class SpaceWritableValidator
    implements ConstraintValidator<SpaceWritable, SpaceForm> {

    private SpaceWritable constraintAnnotation;
    private ContentStoreFactory contentStoreFactory;
    private static Logger log =
        LoggerFactory.getLogger(SpaceWritableValidator.class);

    @Autowired
    public SpaceWritableValidator(ContentStoreFactory contentStoreFactory) {
        this.contentStoreFactory = contentStoreFactory;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(SpaceForm spaceForm,
                           ConstraintValidatorContext context) {
        try {
            // Test validity by attempting to delete a file that does not exist
            ContentStore contentStore =
                contentStoreFactory.create(spaceForm.getCredentialsForm(), false);
            String spaceId = spaceForm.getSpaceId();
            String contentId =
                ".duracloud-dummy-item-" + System.currentTimeMillis();
            contentStore.deleteContent(spaceId, contentId);
            return true;
        } catch (UnauthorizedException e) {
            log.error(e.getMessage(), e);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(constraintAnnotation.message())
                   .addNode("spaceId")
                   .addConstraintViolation();
            return false;
        } catch (NotFoundException ex) {
            return true;
        } catch (Exception ex) {
            throw new RuntimeException("unexpected exception!:" + ex.getMessage(), ex);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.validation.ConstraintValidator#initialize(java.lang.annotation.
     * Annotation)
     */
    @Override
    public void initialize(SpaceWritable constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }
}
