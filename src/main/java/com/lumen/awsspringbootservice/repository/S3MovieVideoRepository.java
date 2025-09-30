package com.lumen.awsspringbootservice.repository;

public interface S3MovieVideoRepository {

    /**
     * Generates a pre-signed GET URL for downloading a video from S3.
     * <p>
     * The generated URL is temporary and allows clients to access
     * the video file directly from S3 without exposing permanent credentials.
     *
     * @param bucketName the name of the S3 bucket containing the video.
     * @param key        the S3 object key that identifies the video.
     * @return a pre-signed URL that can be used to download the video.
     */
    String generateGetPresignedUrl(String bucketName, String key);

    /**
     * Generates a pre-signed PUT URL for uploading a video to S3.
     * <p>
     * The generated URL is temporary and allows clients to upload
     * a video file directly to S3 without using application-side streaming.
     *
     * @param bucketName the name of the S3 bucket where the video will be stored.
     * @param key        the S3 object key under which the video will be saved.
     * @return a pre-signed URL that can be used to upload the video.
     */
    String generatePutPresignedUrl(String bucketName, String key);
}
