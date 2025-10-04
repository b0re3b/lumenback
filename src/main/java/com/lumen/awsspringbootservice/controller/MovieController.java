package com.lumen.awsspringbootservice.controller;

import com.lumen.awsspringbootservice.dto.PageResponse;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.response.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.MovieResponse;
import com.lumen.awsspringbootservice.validator.annotation.ValidImageFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Movies", description = "Movie management APIs")
@RequestMapping("api/v1/lumen/movies")
public interface MovieController {

    @Operation(
            summary = "Get movies with filters and pagination",
            description = "Retrieve a paginated list of movies with optional filters such as title, genre, and premiere date",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved movies",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping
    ResponseEntity<PageResponse<MovieResponse>> getMovies(
            @Parameter(description = "Filter parameters for searching movies",
                    schema = @Schema(implementation = MovieFiltersRequest.class))
            @ModelAttribute MovieFiltersRequest filters,

            @Parameter(description = "Page number (starting from 1)")
            @RequestParam(defaultValue = "1") @Min(1) int page,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") @Min(1) int size
    );

    @Operation(
            summary = "Get movie by ID",
            description = "Retrieve details of a specific movie by its unique identifier",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the movie",
                            content = @Content(schema = @Schema(implementation = MovieDetailsResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Movie not found"
                    )
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<MovieDetailsResponse> getMovie(
            @Parameter(description = "Movie ID", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Create a new movie",
            description = "Create a new movie along with its plans and poster file",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Movie successfully created",
                            content = @Content(schema = @Schema(implementation = MovieDetailsResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error"
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<MovieDetailsResponse> createMovie(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movie creation request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MovieCreationRequest.class))
            )
            @RequestPart("request") @Valid MovieCreationRequest request,
            @RequestPart("posterFile") @NotNull @ValidImageFile MultipartFile posterFile
    ) throws IOException;
}
