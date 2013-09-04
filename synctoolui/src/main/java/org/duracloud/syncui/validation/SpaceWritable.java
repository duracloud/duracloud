/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
                             " administrator.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
