package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.dto.response.MovieUploadUrlsResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.repository.S3MoviePosterRepository;
import com.lumen.awsspringbootservice.repository.S3MovieVideoRepository;
import com.lumen.awsspringbootservice.service.MovieService;
import com.lumen.awsspringbootservice.util.HlsUtils;
import com.lumen.awsspringbootservice.util.MovieSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final MovieMapper movieMapper;

    private final S3MoviePosterRepository s3MoviePosterRepository;

    private final S3MovieVideoRepository s3MovieVideoRepository;

    public MovieDto getMovieById(String movieId) {
        log.info("Fetching movie by id: {}", movieId);
        MovieDto dto = movieMapper.toDto(movieRepository
                .findById(UUID.fromString(movieId))
                .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found")));

        log.info("Movie successfully found: {}", dto);
        return dto;
    }

    @Transactional
    public MovieDto createMovie(MovieCreationRequest request) throws IOException {
        log.info("Creating new movie: {}", request);

        Movie entity = movieRepository.save(movieMapper.toEntity(request));

        String posterS3Key = s3MoviePosterRepository.uploadFile(entity.getId().toString(), request.getPosterFile());

        entity.setPosterS3Key(posterS3Key);

        MovieDto response = movieMapper.toDto(movieRepository.save(entity));

        log.info("Movie successfully created with id: {}", response.getId());
        return response;
    }

    public MovieDto updateMovie(MovieDto dto) {
        log.info("Updating movie: {}", dto);
        Movie existingEntity = movieRepository.findById(UUID.fromString(dto.getId()))
                .orElseThrow(() -> new NotFoundException("Movie with id " + dto.getId() + " not found"));

        Movie entity = movieMapper.toEntity(dto);

        MovieDto response = movieMapper.toDto(movieRepository.save(entity));

        log.info("Movie successfully updated");
        return response;
    }

    public Page<MovieDto> getMovies(MovieFiltersRequest filter, int pageNumber, int pageSize) {
        log.info("Fetching movies with filters: {}, page {}, size {}", filter, pageNumber, pageSize);

        Specification<Movie> spec = Specification.
                allOf(MovieSpecifications.title(filter.getTitle()))
                .and(MovieSpecifications.genre(filter.getGenre()))
                .and(MovieSpecifications.premiereDateAfter(filter.getPremiereDateFrom()))
                .and(MovieSpecifications.premiereDateBefore(filter.getPremiereDateTo()));

        Page<MovieDto> result = movieRepository.findAll(spec, PageRequest.of(pageNumber, pageSize))
                .map(movieMapper::toDto);

        log.info("Found {} movies", result.getTotalElements());
        return result;
    }

    public Page<MovieDto> getMovies(int pageNumber, int pageSize) {
        log.info("Fetching movies, page {}, size {}", pageNumber, pageSize);

        Page<MovieDto> result = movieRepository.findAll(PageRequest.of(pageNumber, pageSize))
                .map(movieMapper::toDto);

        log.info("Found {} movies", result.getTotalElements());
        return result;
    }

    public MovieUploadUrlsResponse createMovieUploadUrls(String movieId, MovieUploadUrlsRequest request) {
        log.info("Fetcing upload presigned urls for movie with id: {}", movieId);

        Movie movie = movieRepository.findById(UUID.fromString(movieId))
                .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found"));

        if (movie.getVideoManifestS3Key() != null ||
                (movie.getVideoFragments() != null && !movie.getVideoFragments().isEmpty())) {
            log.info("Deleting existing video data from S3 for movie id: {}", movieId);
            if (movie.getVideoManifestS3Key() != null) {
                s3MovieVideoRepository.deleteObject(movie.getVideoManifestS3Key());
            }
            if (movie.getVideoFragments() != null) {
                movie.getVideoFragments().forEach(
                        fragment -> s3MovieVideoRepository.deleteObject(fragment.getVideoFragmentS3Key())
                );
            }
            movie.setVideoManifestS3Key(null);
            movie.setVideoFragments(new ArrayList<>());
            movieRepository.save(movie);
        }

        Map<Integer, String> fragmentsDurations = HlsUtils.parseManifest(request.getManifestContent());

        String manifestKey = s3MovieVideoRepository.generateManifestKey(movieId);
        List<String> fragmentsKeys = s3MovieVideoRepository.generateFragmentKeys(movieId, fragmentsDurations.size());

        List<Movie.VideoFragment> fragmentList = new ArrayList<>();
        for (int i = 0; i < fragmentsKeys.size(); i++) {
            Movie.VideoFragment fragment = new Movie.VideoFragment();
            fragment.setVideoFragmentS3Key(fragmentsKeys.get(i));
            fragment.setDuration(fragmentsDurations.get(i));
            fragmentList.add(fragment);
        }

        String mainManifestUrl = s3MovieVideoRepository.generatePutPresignedUrl(manifestKey);
        List<String> segmentsUrls = fragmentsKeys.stream()
                .map(s3MovieVideoRepository::generatePutPresignedUrl)
                .toList();

        movie.setVideoManifestS3Key(manifestKey);
        movie.setVideoFragments(fragmentList);
        movieRepository.save(movie);

        return MovieUploadUrlsResponse.builder()
                .mainManifestUrl(mainManifestUrl)
                .segmentsUrls(segmentsUrls)
                .movieId(movieId)
                .build();

    }

    public String getMovieVideoUrl(String movieId) {
        log.info("Fetching video manifest for movie id: {}", movieId);

        Movie movie = movieRepository.findById(UUID.fromString(movieId))
                .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found"));

        if (movie.getVideoFragments() == null || movie.getVideoFragments().isEmpty()) {
            throw new IllegalStateException("Movie does not have any uploaded video fragments.");
        }

        Map<String, String> fragmentsWithUrls = new LinkedHashMap<>();

        for (Movie.VideoFragment fragment : movie.getVideoFragments()) {
            String presignedUrl = s3MovieVideoRepository.generateGetPresignedUrl(fragment.getVideoFragmentS3Key());
            fragmentsWithUrls.put(presignedUrl, fragment.getDuration());
        }
        return HlsUtils.generateManifest(fragmentsWithUrls);
    }

}
