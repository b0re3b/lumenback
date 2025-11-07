package com.lumen.awsspringbootservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.ses.SesClient;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("test")
public class TestSesConfig {

    @Bean
    @Primary
    public SesClient sesClient() {
        return mock(SesClient.class);
    }
}
