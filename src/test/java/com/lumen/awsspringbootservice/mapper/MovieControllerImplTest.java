package com.lumen.awsspringbootservice.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumen.awsspringbootservice.controller.impl.MovieControllerImpl;
import com.lumen.awsspringbootservice.dto.PageResponse;
import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.response.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.MovieResponse;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.service.MovieService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private MovieMapper movieMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnMovieDetailsResponse_whenMovieExists() throws Exception {
        MovieDto dto = new MovieDto();
        MovieDetailsResponse response = new MovieDetailsResponse();

        Mockito.when(movieService.getMovieById("123")).thenReturn(dto);
        Mockito.when(movieMapper.toDetailsResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/api/v1/lumen/movies/{id}", 123))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnPageResponse_whenRequestingMoviesWithPagination() throws Exception {
        MovieDto dto = new MovieDto();
        MovieResponse response = new MovieResponse();
        PageResponse<MovieResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(response));

        Mockito.when(movieService.getMovies(any(), eq(0), eq(10)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        Mockito.when(movieMapper.toResponse(dto)).thenReturn(response);
        Mockito.when(movieMapper.toPageResponse(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/lumen/movies")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCreateMovieAndReturnDetailsResponse_whenRequestIsValid() throws Exception {
        MockMultipartFile posterFile =
                new MockMultipartFile("posterFile", "poster.jpg", "image/jpeg", "dummy".getBytes());

        MovieCreationRequest request = new MovieCreationRequest();
        request.setAuthor("James Cameron");
        request.setTitle("Avatar");
        request.setDescription("Epic sci-fi movie");
        request.setPremiereDate(LocalDateTime.now().plusDays(30));
        request.setGenres(Set.of(Genre.ACTION));
        request.setMoviePlanShortDtoList(List.of(new MoviePlanShortDto("123", PlanType.MONTH, new BigDecimal(10))));

        MockMultipartFile requestPart =
                new MockMultipartFile("request", "request.json", "application/json",
                        objectMapper.writeValueAsBytes(request));

        MovieDto dto = new MovieDto();
        MovieDetailsResponse response = new MovieDetailsResponse();

        Mockito.when(movieService.createMovie(any())).thenReturn(dto);
        Mockito.when(movieMapper.toDetailsResponse(dto)).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/lumen/movies")
                        .file(requestPart)
                        .file(posterFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());
    }
}