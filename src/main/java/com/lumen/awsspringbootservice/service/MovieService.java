package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.request.MovieFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface MovieService {
    /**
     * Retrieves a movie by its identifier.
     *
     * @param movieId the ID of the movie as a String (UUID format).
     * @return a {@link MovieDto} representing the movie.
     * @throws com.lumen.awsspringbootservice.exception.NotFoundException if no movie with the given ID exists.
     */
    MovieDto getMovieById(String movieId);

    /**
     * Creates a new movie along with its plans.
     *
     * @param dto the {@link MovieDto} containing movie details and plans.
     * @return the created {@link MovieDto} with generated identifier.
     */
    MovieDto createMovie(MovieDto dto);

    /**
     * Updates an existing movie and its related entities.
     *
     * @param dto the {@link MovieDto} containing updated movie details and plans.
     *            Must include a valid identifier of an existing movie.
     * @return the updated {@link MovieDto}.
     * @throws com.lumen.awsspringbootservice.exception.NotFoundException if no movie with the given ID exists.
     */
    MovieDto updateMovie(MovieDto dto);

    /**
     * Retrieves a paginated list of movies based on the provided filter criteria.
     * Supported filters include title, genre, and premiere date ranges.
     *
     * @param filter   the {@link MovieFilterRequest} containing filtering options.
     * @param pageable the {@link Pageable} object specifying pagination and sorting parameters.
     * @return a {@link Page} of {@link MovieDto} matching the filter criteria.
     */
    Page<MovieDto> getMovies(MovieFilterRequest filter, Pageable pageable);

    /**
     * Retrieves a paginated list of all movies without applying any filters.
     *
     * @param pageable the {@link Pageable} object specifying pagination and sorting parameters.
     * @return a {@link Page} of all available {@link MovieDto}.
     */
    Page<MovieDto> getMovies(Pageable pageable);
}
