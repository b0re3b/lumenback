package com.lumen.awsspringbootservice.repository.impl;

import com.lumen.awsspringbootservice.repository.S3MoviePosterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Utilities;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Repository
@Slf4j
@RequiredArgsConstructor
public class S3MoviePosterRepositoryImpl implements S3MoviePosterRepository {
    private final S3Client s3Client;

    @Value("${aws.s3.movie.poster.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.movie.poster.folder.name}")
    private String folderName;

    public String uploadFile(String movieId, MultipartFile file) throws IOException {
        String key = folderName + "/" + movieId + "/" + file.getOriginalFilename() + "." + file.getContentType();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getInputStream().available())
        );

        return key;
    }

    public String generateGetPublicUrl(String key) {
        S3Utilities utilities = s3Client.utilities();
        return utilities.getUrl(builder -> builder.bucket(bucketName).key(key)).toExternalForm();
    }

}
