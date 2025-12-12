package com.deloitte.Auth_Service.config;

import com.deloitte.Auth_Service.util.MultiFormatLocalDateDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * Registers Jackson customizations for the service.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Module multiFormatLocalDateModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalDate.class, new MultiFormatLocalDateDeserializer());
        return module;
    }
}

