package com.lumen.awsspringbootservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumen.awsspringbootservice.controller.impl.MovieControllerImpl;
import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.dto.request.movie.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.movie.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.movie.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.dto.response.PageResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieUploadUrlsResponse;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.service.MovieService;
import com.lumen.awsspringbootservice.service.PurchaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("MovieControllerImpl Tests")
class MovieControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private MovieMapper movieMapper;

    @MockitoBean
    private PurchaseService purchaseService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /movies/{id} should return 200 when movie exists")
    void shouldReturnMovieDetailsResponse_whenMovieExists() throws Exception {
        MovieDto dto = new MovieDto();
        MovieDetailsResponse response = new MovieDetailsResponse();

        when(movieService.getMovieById("123")).thenReturn(dto);
        when(movieMapper.toDetailsResponse(dto)).thenReturn(response);

        mockMvc.perform(get("/api/v1/lumen/movies/{id}", 123))
                .andExpect(status().isOk());

        verify(movieService).getMovieById("123");
        verify(movieMapper).toDetailsResponse(dto);
    }

    @Test
    @DisplayName("GET /movies should return 200 with paginated list")
    void shouldReturnPageResponse_whenRequestingMoviesWithPagination() throws Exception {
        MovieDto dto = new MovieDto();
        MovieResponse response = new MovieResponse();
        PageResponse<MovieResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(response));

        when(movieService.getMovies(any(MovieFiltersRequest.class), eq(0), eq(10)))
                .thenReturn(org.springframework.data.domain.Page.empty());
        when(movieMapper.toPageResponse(any(org.springframework.data.domain.Page.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/lumen/movies")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        verify(movieService).getMovies(any(MovieFiltersRequest.class), eq(0), eq(10));
        verify(movieMapper).toPageResponse(any());
    }

    @Test
    @DisplayName("POST /movies should create a movie and return 201")
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

        when(movieService.createMovie(any())).thenReturn(dto);
        when(movieMapper.toDetailsResponse(dto)).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/lumen/movies")
                        .file(requestPart)
                        .file(posterFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated());

        verify(movieService).createMovie(any(MovieCreationRequest.class));
        verify(movieMapper).toDetailsResponse(dto);
    }

    @Test
    @DisplayName("PUT /movies/{id} should create upload URLs and return 200")
    void shouldCreateMovieUploadUrlsAndReturnResponse() throws Exception {
        String movieId = "abc-123";
        MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
        request.setManifestContent("#EXTM3U\n#EXTINF:10.0,\n0.ts\n#EXT-X-ENDLIST");

        MovieUploadUrlsResponse response = MovieUploadUrlsResponse.builder()
                .movieId(movieId)
                .mainManifestUrl("https://s3.url/manifest")
                .segmentsUrls(List.of("https://s3.url/0.ts"))
                .build();

        when(movieService.createMovieUploadUrls(eq(movieId), any(MovieUploadUrlsRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/lumen/movies/{id}", movieId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(movieService).createMovieUploadUrls(eq(movieId), any(MovieUploadUrlsRequest.class));
    }

    @Test
    @DisplayName("GET /movies/{id}/video-url should return 200 with manifest content")
    void shouldReturnVideoManifest_whenMovieExists() throws Exception {
        String movieId = "abc-456";
        String manifest = "#EXTM3U\n#EXTINF:10.0,\nsegment0.ts\n#EXT-X-ENDLIST";

        when(movieService.getMovieVideoUrl(movieId)).thenReturn(manifest);

        mockMvc.perform(get("/api/v1/lumen/movies/{id}/video-url", movieId))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/vnd.apple.mpegurl"))
                .andExpect(content().string(manifest));

        verify(movieService).getMovieVideoUrl(movieId);
    }

    @Test
    @DisplayName("POST /movies/{id}/{moviePlanId}/purchase should return 201 with payment URL")
    void shouldReturnCreatedPaymentSession_whenPurchaseMovie() throws Exception {
        String movieId = "movie-123";
        String moviePlanId = "plan-456";
        String userId = "user-789";
        String expectedUrl = "https://mock.payment/session123";

        when(purchaseService.createPurchaseSession(movieId, moviePlanId, userId))
                .thenReturn(expectedUrl);

        mockMvc.perform(post("/api/v1/lumen/movies/{id}/{moviePlanId}/purchase", movieId, moviePlanId)
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentUrl").value(expectedUrl));

        verify(purchaseService).createPurchaseSession(movieId, moviePlanId, userId);
    }

    @Test
    @DisplayName("GET /movies/top-sales should return 200 with top-selling movies")
    void shouldReturnTopSalesMovies() throws Exception {
        MovieResponse movieResponse = new MovieResponse();
        movieResponse.setId("m-1");
        PageResponse<MovieResponse> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(movieResponse));

        when(movieService.getTopSalesMovies(eq(0), eq(10)))
                .thenReturn(Page.empty());
        when(movieMapper.toPageResponse(any(Page.class)))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/lumen/movies/top-sales")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(movieService).getTopSalesMovies(0, 10);
        verify(movieMapper).toPageResponse(any());
    }

    @Test
    @DisplayName("GET /movies/top-genres should return 200 with most popular genres")
    void shouldReturnTopGenres() throws Exception {
        PageResponse<Genre> pageResponse = new PageResponse<>();
        pageResponse.setContent(List.of(Genre.ACTION, Genre.DRAMA));

        when(movieService.getTopGenres(eq(0), eq(10)))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/lumen/movies/top-genres")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(movieService).getTopGenres(0, 10);
        verify(movieMapper).toPageResponse(any());
    }
}
