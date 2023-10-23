/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.config;

import org.duracloud.common.aop.RetryAdvice;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.RegexpMethodPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AOP config
 *
 * @author mikejritter
 */
@Configuration
public class AopConfig {

    @Bean
    public RegexpMethodPointcutAdvisor retryAdvisor(final RetryAdvice retryAdvice) {
        final var pattern = ".*StatelessStorageProvider.*";
        return new RegexpMethodPointcutAdvisor(pattern, retryAdvice);
    }

    @Bean
    public RegexpMethodPointcutAdvisor verifySpaceCreationAdvisor(final RetryAdvice verifySpaceCreationAdvice) {
        final var pattern = ".*StatelessStorageProvider.createSpace";
        return new RegexpMethodPointcutAdvisor(pattern, verifySpaceCreationAdvice);
    }

    @Bean
    public RegexpMethodPointcutAdvisor verifySpaceDeletionAdvisor(final RetryAdvice verifySpaceDeletionAdvice) {
        final var pattern = ".*StatelessStorageProvider.deleteSpace";
        return new RegexpMethodPointcutAdvisor(pattern, verifySpaceDeletionAdvice);
    }

    @Bean
    public RegexpMethodPointcutAdvisor snapshotAccessAdvisor(final RetryAdvice snapshotAccessAdvice) {
        final var pattern = ".*TaskProvider.*[.]performTask";
        return new RegexpMethodPointcutAdvisor(pattern, snapshotAccessAdvice);
    }

    @Bean
    public RegexpMethodPointcutAdvisor streamingAccessAdvisor(final RetryAdvice streamingAccessAdvice) {
        final var pattern = ".*TaskProvider.*[.]performTask";
        return new RegexpMethodPointcutAdvisor(pattern, streamingAccessAdvice);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

}
