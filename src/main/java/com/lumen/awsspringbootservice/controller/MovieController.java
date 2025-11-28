package com.lumen.awsspringbootservice.controller;

import com.lumen.awsspringbootservice.dto.request.movie.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.movie.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.movie.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.dto.response.PageResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieUploadUrlsResponse;
import com.lumen.awsspringbootservice.dto.response.purchase.CreatePaymentSessionResponse;
import com.lumen.awsspringbootservice.enums.Genre;
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

    @Operation(
            summary = "Generate pre-signed upload URLs for video fragments",
            description = """
                    Generates pre-signed S3 PUT URLs for uploading HLS video fragments 
                    and the main manifest (.m3u8). 
                    If previous video content exists, it will be removed from S3 
                    before new URLs are generated.""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully generated pre-signed upload URLs",
                            content = @Content(schema = @Schema(implementation = MovieUploadUrlsResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid upload request"),
                    @ApiResponse(responseCode = "404", description = "Movie not found")
            }
    )
    @PutMapping(path = "/{id}")
    ResponseEntity<MovieUploadUrlsResponse> createMovieUploadUrls(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Request payload containing manifest content and fragment information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = MovieUploadUrlsRequest.class))
            )
            @RequestBody @Valid MovieUploadUrlsRequest request,

            @Parameter(description = "Unique movie ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Get pre-signed video playback manifest",
            description = """
                    Returns a dynamically generated HLS (.m3u8) manifest that includes 
                    pre-signed S3 GET URLs for all uploaded video fragments.
                    This endpoint is typically used by the video player to begin streaming.""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully generated and returned HLS manifest",
                            content = @Content(
                                    mediaType = "application/vnd.apple.mpegurl",
                                    schema = @Schema(type = "string", example = "#EXTM3U\\n#EXTINF:10.0,\\nhttps://s3.amazonaws.com/.../0.ts\\n#EXT-X-ENDLIST")
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Movie not found"),
                    @ApiResponse(responseCode = "409", description = "Movie has no uploaded video fragments")
            }
    )
    @GetMapping(path = "/{id}/video-url", produces = "application/vnd.apple.mpegurl")
    ResponseEntity<String> getMovieVideoUrl(
            @Parameter(description = "Unique movie ID", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable("id") String id
    );

    @Operation(
            summary = "Purchase a movie plan",
            description = """
                    Creates a new purchase session for the specified movie and plan.
                    This will generate a Stripe checkout session for the given user.""",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Purchase session successfully created",
                            content = @Content(schema = @Schema(implementation = CreatePaymentSessionResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Movie or plan not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
            }
    )
    @PostMapping("/{id}/{moviePlanId}/purchase")
    ResponseEntity<CreatePaymentSessionResponse> purchaseMovie(
            @Parameter(description = "Movie ID", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable("id") String movieId,

            @Parameter(description = "Movie plan ID", required = true,
                    example = "b2c924c3-ff44-42a0-8f88-9b8bdb46d222")
            @PathVariable("moviePlanId") String moviePlanId,

            @Parameter(description = "User ID who is making the purchase", required = true,
                    example = "user-12345")
            @RequestParam("userId") String userId
    );


    @Operation(
            summary = "Get top-selling movies and genres",
            description = """
                    Retrieves two analytics endpoints:
                    - Top-selling movies by purchase count
                    - Top-selling genres by aggregate revenue or count
                    Used for analytical dashboards or recommendation widgets.""",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved the list",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/top-sales")
    ResponseEntity<PageResponse<MovieResponse>> getTopSalesMovies(
            @Parameter(description = "Page number (starting from 1)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size
    );


    @Operation(
            summary = "Get top-genres by sales",
            description = "Returns a paginated list of the most popular movie genres based on sales statistics.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved top genres",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/top-genres")
    ResponseEntity<PageResponse<Genre>> getTopGenres(
            @Parameter(description = "Page number (starting from 1)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) int page,

            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) int size
    );

}
