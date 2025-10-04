package com.lumen.awsspringbootservice.repository.impl;

import com.lumen.awsspringbootservice.repository.S3MovieVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Repository
@Slf4j
@RequiredArgsConstructor
public class S3MovieVideoRepositoryImpl implements S3MovieVideoRepository {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.movie.video.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.movie.video.folder.name}")
    private String folderName;

    public String generateGetPresignedUrl(String bucketName, String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
    }

    public String generatePutPresignedUrl(String bucketName, String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        return s3Presigner.presignPutObject(putObjectPresignRequest).url().toString();
    }

}
