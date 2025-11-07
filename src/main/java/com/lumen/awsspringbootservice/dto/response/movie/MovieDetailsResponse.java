package com.lumen.awsspringbootservice.dto.response.movie;

import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.enums.Genre;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovieDetailsResponse {
    private String id;

    private String author;

    private String title;

    private String description;

    private Set<Genre> genres = new HashSet<>();

    private List<@Valid MoviePlanShortDto> moviePlans = new ArrayList<>();

    private LocalDateTime premiereDate;

    private String posterUrl;
}
