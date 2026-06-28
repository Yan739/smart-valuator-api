package com.yann.smart_valuator_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * Jackson configuration for JSON serialization/deserialization.
 * Uses tools.jackson (Jackson 3.x), which is the default in Spring Boot 4.
 */
@Configuration
public class JacksonConfig {

    /**
     * Configures the primary ObjectMapper bean.
     * Disables serialization of dates as numeric timestamps,
     * enforcing ISO-8601 string format instead.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Serialize LocalDateTime as ISO-8601 string, not as a timestamp array
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
