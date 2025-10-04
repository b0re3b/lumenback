package com.lumen.awsspringbootservice.repository;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3MoviePosterRepository {

    /**
     * Uploads a poster file for a specific movie to the configured S3 bucket.
     * <p>
     * The file will be stored under a key that usually includes the movie identifier
     * and the original filename. The exact key format is implementation-specific.
     *
     * @param movieId the unique identifier of the movie (used to build the S3 key).
     * @param file    the multipart file representing the poster to upload.
     * @return the generated S3 key under which the file was stored.
     * @throws IOException if reading the file stream fails during upload.
     */
    String uploadFile(String movieId, MultipartFile file) throws IOException;

    /**
     * Generates a public URL for accessing the poster file in S3.
     * <p>
     * Depending on the implementation, this may return:
     * <ul>
     *   <li>a direct S3 URL (e.g., {@code https://bucket.s3.amazonaws.com/key})</li>
     *   <li>or a pre-signed URL with limited validity</li>
     * </ul>
     *
     * @param key the S3 object key of the poster.
     * @return a public URL to access the file.
     */
    String generateGetPublicUrl(String key);
}
