package com.lumen.awsspringbootservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@Profile("test")
public class TestS3Config {

    @Value("${aws.s3.movie.video.bucket.name}")
    private String movieVideoBucketName;

    @Value("${aws.s3.movie.poster.bucket.name}")
    private String moviePosterBucketName;

    @Bean
    public S3Client s3Client(LocalStackContainer localStackContainer) {
        S3Client s3Client = S3Client.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(localStackContainer.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        createBuckets(s3Client);

        return s3Client;
    }

    @Bean
    public S3Presigner s3Presigner(LocalStackContainer localStackContainer) {
        return S3Presigner.builder()
                .endpointOverride(localStackContainer.getEndpointOverride(LocalStackContainer.Service.S3))
                .region(Region.of(localStackContainer.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                localStackContainer.getAccessKey(), localStackContainer.getSecretKey())))
                .build();
    }

    private void createBuckets(S3Client s3Client) {
        createMovieVideoBucket(s3Client);
        createMoviePosterBucket(s3Client);
    }

    private void createMovieVideoBucket(S3Client s3Client) {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(movieVideoBucketName)
                .build());
    }

    private void createMoviePosterBucket(S3Client s3Client) {
        s3Client.createBucket(CreateBucketRequest.builder()
                .bucket(moviePosterBucketName)
                .build());
    }

}
