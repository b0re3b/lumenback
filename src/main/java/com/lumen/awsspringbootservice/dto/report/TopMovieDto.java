package com.lumen.awsspringbootservice.dto.report;

import com.lumen.awsspringbootservice.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TopMovieDto {
    private UUID movieId;
    private String title;
    private Set<Genre> genres;
    private long units;
    private BigDecimal revenue;
}
