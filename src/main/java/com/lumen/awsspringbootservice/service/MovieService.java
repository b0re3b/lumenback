package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import org.springframework.data.domain.Page;

import java.io.IOException;


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
     * Creates a new movie along with its associated plans and uploads
     * related resources (e.g., poster file).
     *
     * @param request the {@link MovieCreationRequest} containing movie details,
     *                plans, and poster file.
     * @return the created {@link MovieDto} with a generated identifier.
     * @throws IOException if reading or uploading the poster file fails.
     */
    MovieDto createMovie(MovieCreationRequest request) throws IOException;

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
     * Retrieves a paginated list of movies filtered by the given criteria.
     *
     * @param filter the {@link MovieFiltersRequest} containing optional filters
     *               such as title, genre, or premiere date range.
     * @param page   the page number (1-based).
     * @param size   the number of items per page.
     * @return a {@link Page} of {@link MovieDto} matching the filter criteria.
     */
    Page<MovieDto> getMovies(MovieFiltersRequest filter, int page, int size);

    /**
     * Retrieves a paginated list of all movies without filters.
     *
     * @param page the page number (1-based).
     * @param size the number of items per page.
     * @return a {@link Page} of {@link MovieDto} containing all movies.
     */
    Page<MovieDto> getMovies(int page, int size);
}
