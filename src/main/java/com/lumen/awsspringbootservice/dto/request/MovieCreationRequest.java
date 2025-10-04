package com.lumen.awsspringbootservice.dto.request;

import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Request payload for creating a new movie with metadata, plans, and poster file")
public class MovieCreationRequest {

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author name must be at most 100 characters")
    @Schema(description = "Author of the movie", example = "Christopher Nolan")
    private String author;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
    @Schema(description = "Movie title", example = "Inception")
    private String title;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    @Schema(description = "Detailed description of the movie", example = "A mind-bending thriller about dreams within dreams.")
    private String description;

    @NotEmpty(message = "Movie must have at least one genre")
    @Schema(description = "Set of genres for the movie", example = "[\"SCIENCE_FICTION\", \"ACTION\"]")
    private Set<Genre> genres = new HashSet<>();

    @NotEmpty(message = "Movie must have at least one plan")
    @Schema(description = "Available subscription plans for the movie")
    private List<@Valid MoviePlanShortDto> moviePlanShortDtoList = new ArrayList<>();

    @NotNull(message = "Premiere date is required")
    @Future(message = "Premiere date cannot be in the past")
    @Schema(description = "Premiere date of the movie", example = "2025-12-15T20:00:00")
    private LocalDateTime premiereDate;

    private MultipartFile posterFile;
}
