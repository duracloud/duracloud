package org.duracloud.syncui.validation;

import java.io.File;
import java.io.FileInputStream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.duracloud.client.ContentStore;
import org.duracloud.syncui.domain.SpaceForm;
import org.duracloud.syncui.service.ContentStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */

public class SpaceWritableValidator
    implements ConstraintValidator<SpaceWritable, SpaceForm> {
    private SpaceWritable constraintAnnotation;
    private ContentStoreFactory contentStoreFactory;
    private static Logger log = LoggerFactory.getLogger(SpaceWritableValidator.class);
    @Autowired
    public SpaceWritableValidator (ContentStoreFactory contentStoreFactory) {
        this.contentStoreFactory = contentStoreFactory;
    }
    /*
     * (non-Javadoc)
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(SpaceForm spaceForm, ConstraintValidatorContext context) {

        File file = null;
        try {
            file = File.createTempFile(System.currentTimeMillis()+"", "tmp");
            file.createNewFile();

            //test validity by attempting to write a tmpfile to the space.
            ContentStore contentStore = contentStoreFactory.create(spaceForm.getCredentialsForm());
            
            String spaceId = spaceForm.getSpaceId();
            String contentId = file.getName();
            FileInputStream content = new FileInputStream(file);
            contentStore.addContent(spaceId, contentId, content, file.length(), "plain/text",null, null);
            contentStore.deleteContent(spaceId,  contentId);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(constraintAnnotation.message())
                   .addNode("spaceId")
                   .addConstraintViolation();
            return false;
        } finally {
            if (file != null){
                file.delete();
            }
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
