/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.config;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.service.DefaultConversionService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

/**
 * The root application configuration class.
 * @author Daniel Bernstein
 *
 */
@Configuration
@ImportResource(value="/WEB-INF/config/webflow-config.xml")
@ComponentScan( basePackages = {"org.duracloud.syncui"} )
public class AppConfig extends WebMvcConfigurationSupport {
   
   @Bean
   public static PropertyPlaceholderConfigurer properties(){
      PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
      final Resource[] resources = new ClassPathResource[ ] { 
         //new ClassPathResource( "properties1.properties" ),  
         //new ClassPathResource( "properties2.properties" ) 
      };  
      ppc.setLocations( resources );
      ppc.setIgnoreUnresolvablePlaceholders( true );
      return ppc;
   } 
 
   @Bean
   public MessageSource messageSource(){
       ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
       ms.setBasename("classpath:messages");
       ms.setDefaultEncoding("UTF-8");
       ms.setCacheSeconds(5);
       return ms;
   }
   
   @Bean
   public MessageSource localeChangeInterceptor(){
       return messageSource();
   }

   @Override
   @Bean
   public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
       RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
       ConfigurableWebBindingInitializer initializer = 
           (ConfigurableWebBindingInitializer) adapter.getWebBindingInitializer();
       
       PropertyEditorRegistrar propertyEditorRegistrar = new PropertyEditorRegistrar() {
           @Override
           public void registerCustomEditors(PropertyEditorRegistry registry) {
               //Trim strings before setting values on all form beans.
               registry.registerCustomEditor(Object.class, new StringTrimmerEditor(true));
           }
       };

       initializer.setPropertyEditorRegistrar(propertyEditorRegistrar);
       return adapter;
   }
   
   @Bean
   public DefaultFormattingConversionService defaultFormattingConversionService() {
       return new ApplicationFormattingConversionService();
   }
   
   @Bean ConversionService defaultConversionService() {
       return new DefaultConversionService(defaultFormattingConversionService());
   }
   
}
