package org.nastya.config;

import com.fasterxml.jackson.core.JsonFactory;
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
    public ObjectMapper myObjectMapper() {
        JsonFactory factory = JsonFactory.builder()
                .disable(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW)
                .build();

        return new ObjectMapper(factory);
    }
}