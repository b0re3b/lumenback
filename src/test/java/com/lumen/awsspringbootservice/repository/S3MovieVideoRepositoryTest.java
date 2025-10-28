package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.config.TestContainerConfig;
import com.lumen.awsspringbootservice.config.TestS3Config;
import com.lumen.awsspringbootservice.repository.impl.S3MovieVideoRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        S3MovieVideoRepositoryImpl.class,
        TestContainerConfig.class,
        TestS3Config.class,
})
@ActiveProfiles("test")
@DisplayName("S3MovieVideoRepositoryImpl Integration Tests")
class S3MovieVideoRepositoryTest {

    @Autowired
    private S3MovieVideoRepositoryImpl repository;

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.movie.video.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.movie.video.folder.name}")
    private String folderName;

    @Test
    @DisplayName("Should generate valid presigned GET and PUT URLs")
    void shouldGeneratePresignedUrls() {
        String key = folderName + "/movie-123/index.m3u8";

        String getUrl = repository.generateGetPresignedUrl(key);
        String putUrl = repository.generatePutPresignedUrl(key);

        assertNotNull(getUrl);
        assertNotNull(putUrl);
        assertTrue(getUrl.contains(bucketName));
        assertTrue(putUrl.contains(bucketName));
    }

    @Test
    @DisplayName("Should generate correct manifest and fragment keys")
    void shouldGenerateCorrectKeys() {
        String movieId = "movie-xyz";

        String manifestKey = repository.generateManifestKey(movieId);
        assertEquals(folderName + "/" + movieId + "/index.m3u8", manifestKey);

        String fragmentKey = repository.generateFragmentKey(movieId, 5);
        assertEquals(folderName + "/" + movieId + "/5.ts", fragmentKey);

        List<String> fragmentKeys = repository.generateFragmentKeys(movieId, 3);
        assertEquals(3, fragmentKeys.size());
        assertEquals(folderName + "/" + movieId + "/0.ts", fragmentKeys.get(0));
        assertEquals(folderName + "/" + movieId + "/2.ts", fragmentKeys.get(2));
    }

    @Test
    @DisplayName("Should delete existing object and skip non-existing one gracefully")
    void shouldDeleteExistingAndSkipMissingObject() {
        String key = folderName + "/movie-111/test-fragment.ts";

        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(key).build(),
                software.amazon.awssdk.core.sync.RequestBody.fromString("test-data", StandardCharsets.UTF_8)
        );

        assertDoesNotThrow(() -> repository.deleteObject(key));
        assertDoesNotThrow(() -> repository.deleteObject(key));
        assertDoesNotThrow(() -> repository.deleteObject(folderName + "/movie-999/missing.ts"));
    }

}
