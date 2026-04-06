package org.nastya.config;

import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.nastya.service.ImportService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class DeviceParserConfig{

    @Bean
    ApplicationRunner run(ImportService importService, SpringLiquibase liquibase) throws LiquibaseException {
        liquibase.afterPropertiesSet();
        return args -> importService.importJson("device_config.json");
    }

    @Bean
    ObjectMapper myObjectMapper() {
        return new ObjectMapper();
    }
}