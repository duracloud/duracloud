/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.config;

import java.util.List;

import org.duracloud.duradmin.view.BaseViewPreparer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;
import org.springframework.web.servlet.view.XmlViewResolver;
import org.springframework.web.servlet.view.json.error.HttpStatusError;
import org.springframework.web.servlet.view.json.error.ModelFlagError;
import org.springframework.web.servlet.view.json.exception.ExceptionMessageExceptionHandler;
import org.springframework.web.servlet.view.json.exception.JsonExceptionResolver;
import org.springframework.web.servlet.view.json.exception.StackTraceExceptionHandler;
import org.springframework.web.servlet.view.tiles3.SpringBeanPreparerFactory;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesView;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;

/**
 * MVC config
 *
 * @author mikejritter
 */
@Configuration
public class WebMVCConfig {

    @Bean
    public TilesConfigurer tilesConfigurer() {
        final var configurer = new TilesConfigurer();
        configurer.setPreparerFactoryClass(SpringBeanPreparerFactory.class);
        configurer.setDefinitions("WEB-INF/config/tiles.xml");
        return configurer;
    }

    @Bean
    public TilesViewResolver tilesViewResolver() {
        final var resolver = new TilesViewResolver();
        resolver.setViewClass(TilesView.class);
        resolver.setOrder(1);
        return resolver;
    }

    @Bean
    public BaseViewPreparer basePreparer() {
        return new BaseViewPreparer();
    }

    @Bean
    public HttpStatusError statusError() {
        return new HttpStatusError();
    }

    @Bean
    public ModelFlagError modelFlagError() {
        return new ModelFlagError();
    }

    @Bean
    public ExceptionMessageExceptionHandler exceptionMessageExceptionHandler() {
        return new ExceptionMessageExceptionHandler();
    }

    @Bean
    public StackTraceExceptionHandler stackTraceExceptionHandler() {
        return new StackTraceExceptionHandler();
    }

    @Bean
    public JsonExceptionResolver exceptionResolver(final HttpStatusError statusError,
        final ModelFlagError modelFlagError,
        final ExceptionMessageExceptionHandler exceptionMessageExceptionHandler,
        final StackTraceExceptionHandler stackTraceExceptionHandler) {
        final var resolver = new JsonExceptionResolver();
        resolver.setErrorHandler(List.of(statusError, modelFlagError));
        resolver.setExceptionHandler(List.of(exceptionMessageExceptionHandler, stackTraceExceptionHandler));
        return resolver;
    }

    @Bean
    public SimpleControllerHandlerAdapter simpleControllerHandlerAdapter() {
        return new SimpleControllerHandlerAdapter();
    }

    @Bean
    public ViewResolver viewResolver() {
        final var viewResolver = new XmlViewResolver();
        viewResolver.setOrder(0);
        return viewResolver;
    }
}
