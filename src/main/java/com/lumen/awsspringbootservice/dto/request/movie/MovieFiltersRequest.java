package com.lumen.awsspringbootservice.dto.request.movie;

import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Filters for searching movies with pagination")
public class MovieFiltersRequest {

    @Schema(description = "Movie title substring for search", example = "Rings")
    private String title;

    @Schema(description = "Movie author substring for search", example = "John Martin")
    private String author;

    @Schema(description = "List of genres to filter by (movie must contain all)", example = "[\"DRAMA\", \"ACTION\"]")
    private List<Genre> genres;

    @Schema(description = "List of plan types to filter by (movie must contain all)", example = "[\"MONTH\", \"WEEK\"]")
    private List<PlanType> planTypes;

    @Schema(description = "Minimum average rating for the movie", example = "4.5")
    private Double minRating;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Filter for movies with premiere date after this date", example = "2023-01-01")
    private LocalDate premiereDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Filter for movies with premiere date before this date", example = "2025-12-31")
    private LocalDate premiereDateTo;
}

