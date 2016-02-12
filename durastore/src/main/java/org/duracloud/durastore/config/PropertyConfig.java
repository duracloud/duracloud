package org.duracloud.durastore.config;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@PropertySource("file://${duracloud.config.file}") //this references the system property.
public class PropertyConfig {
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException{
        PropertySourcesPlaceholderConfigurer p = new PropertySourcesPlaceholderConfigurer();
        return p;
    }
}
