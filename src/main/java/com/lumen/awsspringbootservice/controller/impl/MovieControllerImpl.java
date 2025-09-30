package com.lumen.awsspringbootservice.controller.impl;

import com.lumen.awsspringbootservice.controller.MovieController;
import com.lumen.awsspringbootservice.dto.PageResponse;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.response.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.MovieResponse;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.service.MovieService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/lumen/movies")
public class MovieControllerImpl implements MovieController {

    private final MovieService movieService;

    private final MovieMapper movieMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<PageResponse<MovieResponse>> getMovies(
            @ModelAttribute MovieFiltersRequest filters,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size

    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                movieMapper.toPageResponse(
                        movieService.getMovies(filters, page - 1, size)
                                .map(movieMapper::toResponse)
                )
        );
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<MovieDetailsResponse> getMovie(
            @PathVariable("id") String id
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(
                movieMapper.toDetailsResponse(
                        movieService.getMovieById(id)
                )
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<MovieDetailsResponse> createMovie(
            @RequestPart("request") @Valid MovieCreationRequest request,
            @RequestPart("posterFile") MultipartFile posterFile
    ) throws IOException {
        request.setPosterFile(posterFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                movieMapper.toDetailsResponse(
                        movieService.createMovie(request)
                )
        );
    }
}
