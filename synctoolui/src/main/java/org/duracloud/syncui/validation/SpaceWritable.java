package org.duracloud.syncui.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;


/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 *
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME) 
@Constraint(validatedBy=SpaceWritableValidator.class)
public @interface SpaceWritable {
    String message() default "You do not have write access to this space. " + 
                             "Please choose another or contact your DuraCloud" +
                             " admin.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
