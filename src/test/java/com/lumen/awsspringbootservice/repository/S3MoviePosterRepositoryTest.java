package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.config.TestContainerConfig;
import com.lumen.awsspringbootservice.config.TestS3Config;
import com.lumen.awsspringbootservice.repository.impl.S3MoviePosterRepositoryImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {
        S3MoviePosterRepositoryImpl.class,
        TestContainerConfig.class,
        TestS3Config.class,
})
@ActiveProfiles("test")
@DisplayName("S3MoviePosterRepositoryImpl Integration Tests")
class S3MoviePosterRepositoryTest {

    @Value("${aws.s3.movie.poster.bucket.name}")
    private String bucketName;

    @Autowired
    private S3MoviePosterRepositoryImpl repository;

    @Autowired
    private S3Client s3Client;


    @Test
    @DisplayName("Should upload file and generate valid public URL")
    void shouldUploadAndGenerateValidPublicUrl() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "poster.jpg", "image/jpeg", "fake-image-content".getBytes()
        );
        String movieId = "abc-movie";
        String key = repository.uploadFile(movieId, file);

        assertNotNull(key);
        assertTrue(key.contains(movieId));

        // when
        String url = repository.generateGetPublicUrl(key);

        // then
        assertNotNull(url);
        assertTrue(url.contains(bucketName));
        assertTrue(url.contains(key));
    }

}
