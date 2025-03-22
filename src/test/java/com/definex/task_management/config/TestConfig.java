package com.definex.task_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Properties;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        
        try {
            Resource resource = new ClassPathResource(".env.test");
            Properties props = new Properties();
            props.load(resource.getInputStream());
            
            configurer.setProperties(props);
            configurer.setLocalOverride(true);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test environment properties", e);
        }
        
        return configurer;
    }
} 