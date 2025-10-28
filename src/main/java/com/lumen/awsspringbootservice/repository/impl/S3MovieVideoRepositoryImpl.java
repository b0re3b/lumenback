package com.lumen.awsspringbootservice.repository.impl;

import com.lumen.awsspringbootservice.repository.S3MovieVideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class S3MovieVideoRepositoryImpl implements S3MovieVideoRepository {

    private final S3Presigner s3Presigner;

    private final S3Client s3Client;

    @Value("${aws.s3.movie.video.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.movie.video.folder.name}")
    private String folderName;

    public String generateGetPresignedUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(getObjectRequest)
                .signatureDuration(Duration.ofMinutes(30))
                .build();

        return s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
    }

    public String generatePutPresignedUrl(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key).build();

        PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(30))
                .build();

        return s3Presigner.presignPutObject(putObjectPresignRequest).url().toString();
    }

    public String generateManifestKey(String movieId) {
        return folderName + "/" + movieId + "/index.m3u8";
    }

    public String generateFragmentKey(String movieId, int fragmentIndex) {
        return folderName + "/" + movieId + "/" + fragmentIndex + ".ts";
    }

    public List<String> generateFragmentKeys(String movieId, int fragmentCount) {
        List<String> keys = new ArrayList<>();
        for (int fragmentIndex = 0; fragmentIndex < fragmentCount; fragmentIndex++) {
            keys.add(generateFragmentKey(movieId, fragmentIndex));
        }
        return keys;
    }

    public void deleteObject(String key) {
        HeadObjectRequest headRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        try {
            s3Client.headObject(headRequest);

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Deleted S3 object with key: {}", key);

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                log.warn("S3 object with key {} does not exist, skipping deletion", key);
            } else {
                throw e;
            }
        }
    }


}
