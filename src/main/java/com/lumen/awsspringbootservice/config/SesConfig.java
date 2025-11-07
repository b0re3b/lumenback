package com.lumen.awsspringbootservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

import java.net.URI;

@Configuration
public class SesConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.credentials.access.key}")
    private String accessKey;

    @Value("${aws.credentials.secret.key}")
    private String secretKey;

    @Value("${aws.ses.endpoint}")
    private String sesEndpoint;

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(sesEndpoint))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)
                        )
                )
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }
}