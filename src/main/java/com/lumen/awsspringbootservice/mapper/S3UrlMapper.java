package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.repository.S3MoviePosterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3UrlMapper {
    private final S3MoviePosterRepository s3MoviePosterRepository;

    @PosterUrlMapping
    public String toPosterUrl(String posterS3Key) {
        return posterS3Key == null ? null : s3MoviePosterRepository.generateGetPublicUrl(posterS3Key);
    }

}
