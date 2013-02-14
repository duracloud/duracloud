/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.domain;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Daniel Bernstein
 *
 */
@Target({ElementType.TYPE}) 
@Retention(RetentionPolicy.RUNTIME) 
@Constraint(validatedBy=DuracloudCredentialsCoherenceCheckerValidator.class)
public @interface DuracloudCredentialsCoherenceChecker {
    String message() default "A DuraCloud instance matching your host, username, and password could not be found. " +
    		"Please try reentering your credentials.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
