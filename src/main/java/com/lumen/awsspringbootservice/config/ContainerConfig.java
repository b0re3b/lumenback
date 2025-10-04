package com.lumen.awsspringbootservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class ContainerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
                .withServices(LocalStackContainer.Service.S3);
    }
}
