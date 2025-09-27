package com.lumen.awsspringbootservice.service.impl;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.request.MovieFilterRequest;
import com.lumen.awsspringbootservice.service.MovieService;
import com.lumen.awsspringbootservice.util.MovieSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final MovieMapper movieMapper;

    public MovieDto getMovieById(String movieId) {
        log.info("Fetching movie by id: {}", movieId);
        MovieDto dto = movieMapper.toDto(movieRepository
                .findById(UUID.fromString(movieId))
                .orElseThrow(() -> new NotFoundException("Movie with id " + movieId + " not found")));

        log.info("Movie successfully found: {}", dto);
        return dto;
    }

    public MovieDto createMovie(MovieDto dto) {
        log.info("Creating new movie: {}", dto);
        Movie entity = movieMapper.toEntity(dto);

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

    public Page<MovieDto> getMovies(MovieFilterRequest filter, Pageable pageable) {
        log.info("Fetching movies with filters: {}, page {}, size {}", filter, pageable.getPageNumber(), pageable.getPageSize());

        Specification<Movie> spec = Specification.
                allOf(MovieSpecifications.title(filter.getTitle()))
                .and(MovieSpecifications.genre(filter.getGenre()))
                .and(MovieSpecifications.premiereDateAfter(filter.getPremiereDateFrom()))
                .and(MovieSpecifications.premiereDateBefore(filter.getPremiereDateTo()));

        Page<MovieDto> result = movieRepository.findAll(spec, pageable)
                .map(movieMapper::toDto);

        log.info("Found {} movies", result.getTotalElements());
        return result;
    }

    public Page<MovieDto> getMovies(Pageable pageable) {
        log.info("Fetching movies, page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<MovieDto> result = movieRepository.findAll(pageable)
                .map(movieMapper::toDto);

        log.info("Found {} movies", result.getTotalElements());
        return result;
    }

}
