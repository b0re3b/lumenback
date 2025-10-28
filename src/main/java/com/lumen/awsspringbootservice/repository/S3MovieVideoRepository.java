package com.lumen.awsspringbootservice.repository;

import java.util.List;

public interface S3MovieVideoRepository {
    /**
     * Generates a pre-signed GET URL for downloading an object from S3.
     * <p>
     * The URL allows temporary read access to a specific S3 object,
     * usually the video fragment or manifest file.
     *
     * @param key the S3 object key (full path including folder and filename)
     * @return a pre-signed GET URL that can be used to download the file
     */
    String generateGetPresignedUrl(String key);

    /**
     * Generates a pre-signed PUT URL for uploading an object to S3.
     * <p>
     * The URL allows temporary write access to upload a file directly to S3,
     * without exposing permanent credentials.
     *
     * @param key the S3 object key (full path where the file will be uploaded)
     * @return a pre-signed PUT URL that can be used to upload the file
     */
    String generatePutPresignedUrl(String key);

    /**
     * Builds the S3 object key for the manifest file (.m3u8) of a movie.
     * <p>
     * The manifest is typically stored under:
     * {@code <folderName>/<movieId>/index.m3u8}
     *
     * @param movieId the unique identifier of the movie
     * @return the generated manifest S3 key
     */
    String generateManifestKey(String movieId);

    /**
     * Builds the S3 object key for a specific video fragment (.ts) of a movie.
     * <p>
     * The fragment is typically stored under:
     * {@code <folderName>/<movieId>/<fragmentIndex>.ts}
     *
     * @param movieId       the unique identifier of the movie
     * @param fragmentIndex the sequential index of the fragment
     * @return the generated fragment S3 key
     */
    String generateFragmentKey(String movieId, int fragmentIndex);

    /**
     * Generates a list of S3 object keys for all video fragments of a movie.
     * <p>
     * Useful when preparing batch uploads of multiple video segments.
     *
     * @param movieId       the unique identifier of the movie
     * @param fragmentCount the total number of fragments
     * @return list of S3 keys, ordered by fragment index (0..fragmentCount-1)
     */
    List<String> generateFragmentKeys(String movieId, int fragmentCount);

    /**
     * Deletes an object from the S3 bucket if it exists.
     * <p>
     * If the object does not exist, the operation is skipped safely.
     * Logs are written at the info/warn level to indicate result.
     *
     * @param key the S3 object key to delete
     */
    void deleteObject(String key);
}
