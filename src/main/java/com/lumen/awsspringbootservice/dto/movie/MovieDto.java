package com.lumen.awsspringbootservice.dto.movie;

import com.lumen.awsspringbootservice.enums.Genre;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
public class MovieDto {

    private String id;

    @NotBlank(message = "Author must not be blank")
    @Size(max = 100, message = "Author name must be at most 100 characters")
    private String author;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 2, max = 150, message = "Title must be between 2 and 150 characters")
    private String title;

    @NotBlank(message = "Description must not be blank")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotEmpty(message = "Movie must have at least one genre")
    private Set<Genre> genres = new HashSet<>();

    @NotEmpty(message = "Movie must have at least one plan")
    private List<@Valid MoviePlanShortDto> moviePlanShortDtoList = new ArrayList<>();

    @NotNull(message = "Premiere date is required")
    @FutureOrPresent(message = "Premiere date cannot be in the past")
    private LocalDateTime premiereDate;

    @NotBlank(message = "S3 key must not be blank")
    private String s3Key;

    @NotBlank(message = "Photo URL must not be blank")
    @Pattern(
            regexp = "^(https?|ftp)://.*$",
            message = "Photo URL must be a valid URL"
    )
    private String photoUrl;
}
