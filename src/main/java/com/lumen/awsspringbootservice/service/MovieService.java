package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.dto.response.MovieUploadUrlsResponse;
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
     * related resources such as a poster image to AWS S3.
     *
     * @param request the {@link MovieCreationRequest} containing movie metadata,
     *                pricing plans, and the poster file to upload.
     * @return the created {@link MovieDto} including the generated movie ID and stored S3 keys.
     * @throws IOException if reading or uploading the poster file fails.
     */
    MovieDto createMovie(MovieCreationRequest request) throws IOException;

    /**
     * Updates an existing movie entity and its associated data.
     * <p>
     * Used for editing movie metadata, plans, or other fields already persisted in the database.
     *
     * @param dto the {@link MovieDto} containing updated movie data;
     *            must include a valid existing movie identifier.
     * @return the updated {@link MovieDto}.
     * @throws com.lumen.awsspringbootservice.exception.NotFoundException if no movie with the provided ID exists.
     */
    MovieDto updateMovie(MovieDto dto);

    /**
     * Retrieves a paginated list of movies filtered by the provided criteria.
     * <p>
     * The filter may include parameters such as title, genre, or premiere date range.
     *
     * @param filter the {@link MovieFiltersRequest} specifying optional filters.
     * @param page   the page number (1-based).
     * @param size   the number of items per page.
     * @return a {@link Page} of {@link MovieDto} matching the given filter.
     */
    Page<MovieDto> getMovies(MovieFiltersRequest filter, int page, int size);

    /**
     * Retrieves a paginated list of all available movies without applying any filters.
     *
     * @param page the page number (1-based).
     * @param size the number of items per page.
     * @return a {@link Page} of {@link MovieDto} containing all movies.
     */
    Page<MovieDto> getMovies(int page, int size);

    /**
     * Generates presigned AWS S3 upload URLs for uploading
     * HLS video fragments and manifest files associated with a movie.
     * <p>
     * This operation also removes any previously uploaded video content for the movie
     * to prevent conflicts or stale data in S3.
     *
     * @param movieId the ID of the target movie (UUID as String).
     * @param request the {@link MovieUploadUrlsRequest} containing parsed manifest data
     *                and information about the fragments to upload.
     * @return a {@link MovieUploadUrlsResponse} containing presigned URLs for the manifest
     * and video fragments.
     * @throws com.lumen.awsspringbootservice.exception.NotFoundException if the movie with the specified ID does not exist.
     */
    MovieUploadUrlsResponse createMovieUploadUrls(String movieId, MovieUploadUrlsRequest request);

    /**
     * Retrieves the HLS manifest (.m3u8) content for a movie’s video.
     * <p>
     * The manifest is dynamically generated using presigned GET URLs
     * for each video fragment stored in AWS S3.
     *
     * @param movieId the ID of the movie whose video should be streamed (UUID as String).
     * @return a String representing the generated HLS manifest file content.
     * @throws com.lumen.awsspringbootservice.exception.NotFoundException if no movie with the given ID exists.
     * @throws IllegalStateException                                      if the movie has no associated video fragments.
     */
    String getMovieVideoUrl(String movieId);
}
