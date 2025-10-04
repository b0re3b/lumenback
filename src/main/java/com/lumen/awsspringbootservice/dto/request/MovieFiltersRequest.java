package com.lumen.awsspringbootservice.dto.request;

import com.lumen.awsspringbootservice.enums.Genre;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Filters for searching movies with pagination")
public class MovieFiltersRequest {

    @Schema(description = "Movie title substring for search", example = "Rings")
    private String title;

    @Schema(description = "Filter by movie genre", example = "DRAMA")
    private Genre genre;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Filter for movies with premiere date after this date", example = "2023-01-01")
    private LocalDate premiereDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Filter for movies with premiere date before this date", example = "2025-12-31")
    private LocalDate premiereDateTo;
}

