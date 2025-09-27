package com.lumen.awsspringbootservice.request;

import com.lumen.awsspringbootservice.enums.Genre;
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
public class MovieFilterRequest {
    private String title;
    private Genre genre;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate premiereDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate premiereDateTo;
}
