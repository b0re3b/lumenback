package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.repository.S3MoviePosterRepository;
import com.lumen.awsspringbootservice.repository.S3MovieVideoRepository;
import com.lumen.awsspringbootservice.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieServiceImpl Unit Tests")
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

    @Mock
    private S3MoviePosterRepository s3MoviePosterRepository;

    @Mock
    private S3MovieVideoRepository s3MovieVideoRepository;


    @InjectMocks
    private MovieServiceImpl movieService;

    @Nested
    @DisplayName("getMovieById Tests")
    class GetMovieByIdTests {

        @Test
        @DisplayName("Should return MovieDto when movie exists")
        void shouldReturnMovieDtoWhenExists() {
            // given
            UUID movieId = UUID.randomUUID();
            Movie movie = new Movie();
            MovieDto dto = new MovieDto();
            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(movieMapper.toDto(movie)).thenReturn(dto);

            // when
            MovieDto result = movieService.getMovieById(movieId.toString());

            // then
            assertNotNull(result);
            verify(movieRepository).findById(movieId);
            verify(movieMapper).toDto(movie);
        }

        @Test
        @DisplayName("Should throw NotFoundException when movie does not exist")
        void shouldThrowNotFoundWhenNotExists() {
            // given
            UUID movieId = UUID.randomUUID();
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            // then
            assertThrows(NotFoundException.class,
                    () -> movieService.getMovieById(movieId.toString()));
        }
    }

    @Nested
    @DisplayName("createMovie Tests")
    class CreateMovieTests {

        @Test
        @DisplayName("Should save and return MovieDto")
        void shouldSaveAndReturnMovieDto() throws IOException {
            // given
            MovieCreationRequest request = new MovieCreationRequest();
            Movie entity = Movie.builder().id(UUID.randomUUID()).build();
            Movie savedEntity = Movie.builder().id(UUID.randomUUID()).build();
            MovieDto savedDto = new MovieDto();

            when(movieMapper.toEntity(request)).thenReturn(entity);
            when(movieRepository.save(any())).thenReturn(savedEntity);
            when(movieMapper.toDto(savedEntity)).thenReturn(savedDto);
            when(s3MoviePosterRepository.uploadFile(any(), any())).thenReturn("key");

            // when
            MovieDto result = movieService.createMovie(request);

            // then
            assertNotNull(result);
            assertEquals(savedDto, result);
            verify(movieMapper).toEntity(request);
            verify(movieRepository).save(entity);
            verify(movieMapper).toDto(savedEntity);
        }
    }

    @Nested
    @DisplayName("updateMovie Tests")
    class UpdateMovieTests {

        @Test
        @DisplayName("Should update and return MovieDto when movie exists")
        void shouldUpdateMovieWhenExists() {
            // given
            UUID movieId = UUID.randomUUID();
            MovieDto dto = new MovieDto();
            dto.setId(movieId.toString());
            Movie existingEntity = new Movie();
            Movie entity = new Movie();
            Movie savedEntity = new Movie();
            MovieDto savedDto = new MovieDto();

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(existingEntity));
            when(movieMapper.toEntity(dto)).thenReturn(entity);
            when(movieRepository.save(entity)).thenReturn(savedEntity);
            when(movieMapper.toDto(savedEntity)).thenReturn(savedDto);

            // when
            MovieDto result = movieService.updateMovie(dto);

            // then
            assertNotNull(result);
            assertEquals(savedDto, result);
            verify(movieRepository).findById(movieId);
            verify(movieMapper).toEntity(dto);
            verify(movieRepository).save(entity);
            verify(movieMapper).toDto(savedEntity);
        }

        @Test
        @DisplayName("Should throw NotFoundException when movie does not exist")
        void shouldThrowNotFoundWhenUpdatingNonexistentMovie() {
            // given
            UUID movieId = UUID.randomUUID();
            MovieDto dto = new MovieDto();
            dto.setId(movieId.toString());

            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            // then
            assertThrows(NotFoundException.class, () -> movieService.updateMovie(dto));
            verify(movieRepository).findById(movieId);
        }
    }

    @Nested
    @DisplayName("getMovies with filter Tests")
    class GetMoviesWithFilterTests {

        @Test
        @DisplayName("Should return movies page when filter applied")
        void shouldReturnMoviesWithFilter() {
            // given
            MovieFiltersRequest filter = new MovieFiltersRequest();
            filter.setTitle("test");

            Movie entity = new Movie();
            entity.setId(UUID.randomUUID());
            MovieDto dto = new MovieDto();
            dto.setId(entity.getId().toString());

            PageRequest pageable = PageRequest.of(0, 5);
            Page<Movie> entitiesPage = new PageImpl<>(List.of(entity));
            Page<MovieDto> expectedPage = new PageImpl<>(List.of(dto));

            when(movieRepository.findAll(any(Specification.class), any(PageRequest.class)))
                    .thenReturn(entitiesPage);
            when(movieMapper.toDto(entity)).thenReturn(dto);

            // when
            Page<MovieDto> result = movieService.getMovies(filter, pageable.getPageNumber(), pageable.getPageSize());

            // then
            assertNotNull(result);
            assertEquals(expectedPage.getContent().size(), result.getContent().size());
            assertEquals(dto.getId(), result.getContent().get(0).getId());

            verify(movieRepository).findAll(any(Specification.class), any(PageRequest.class));
            verify(movieMapper).toDto(entity);
        }
    }

    @Nested
    @DisplayName("getMovies without filter Tests")
    class GetMoviesWithoutFilterTests {

        @Test
        @DisplayName("Should return movies page without filter")
        void shouldReturnMoviesWithoutFilter() {
            // given
            Movie entity = new Movie();
            entity.setId(UUID.randomUUID());
            MovieDto dto = new MovieDto();
            dto.setId(entity.getId().toString());

            PageRequest pageable = PageRequest.of(0, 5);
            Page<Movie> entitiesPage = new PageImpl<>(List.of(entity));
            Page<MovieDto> expectedPage = new PageImpl<>(List.of(dto));

            when(movieRepository.findAll(pageable)).thenReturn(entitiesPage);
            when(movieMapper.toDto(entity)).thenReturn(dto);

            // when
            Page<MovieDto> result = movieService.getMovies(pageable.getPageNumber(), pageable.getPageSize());

            // then
            assertNotNull(result);
            assertEquals(expectedPage.getContent().size(), result.getContent().size());
            assertEquals(dto.getId(), result.getContent().get(0).getId());

            verify(movieRepository).findAll(pageable);
            verify(movieMapper).toDto(entity);
        }
    }

    @Nested
    @DisplayName("createMovieUploadUrls Tests")
    class CreateMovieUploadUrlsTests {

        @Test
        @DisplayName("Should generate upload URLs and persist manifest/fragments when previous video data is empty")
        void shouldGenerateUploadUrlsAndPersistVideoData() {
            // given
            UUID movieId = UUID.randomUUID();
            String movieIdStr = movieId.toString();

            Movie movie = new Movie();
            movie.setId(movieId);
            movie.setVideoManifestS3Key(null);
            movie.setVideoFragments(new LinkedHashMap<>());

            String manifestContent = """
                    #EXTM3U
                    #EXT-X-VERSION:3
                    #EXTINF:10.0,
                    0.ts
                    #EXTINF:12.5,
                    1.ts
                    #EXT-X-ENDLIST
                    """;

            MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
            request.setManifestContent(manifestContent);

            final java.util.List<String> generatedKeysHolder = new java.util.ArrayList<>();

            String manifestKey = "videos/" + movieIdStr + "/index.m3u8";

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(s3MovieVideoRepository.generateManifestKey(movieIdStr)).thenReturn(manifestKey);
            when(s3MovieVideoRepository.generateFragmentKeys(eq(movieIdStr), anyInt()))
                    .thenAnswer(inv -> {
                        int count = inv.getArgument(1, Integer.class);
                        java.util.List<String> keys = new java.util.ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            keys.add("videos/" + movieIdStr + "/" + i + ".ts");
                        }
                        generatedKeysHolder.clear();
                        generatedKeysHolder.addAll(keys);
                        return keys;
                    });
            when(s3MovieVideoRepository.generatePutPresignedUrl(any()))
                    .thenAnswer(inv -> "https://presign-put/" + inv.getArgument(0, String.class));

            // when
            var response = movieService.createMovieUploadUrls(movieIdStr, request);

            // then
            assertNotNull(response);
            assertEquals(movieIdStr, response.getMovieId());
            assertNotNull(response.getMainManifestUrl());
            assertTrue(response.getMainManifestUrl().startsWith("https://presign-put/"));
            assertEquals(2, response.getSegmentsUrls().size());
            for (String key : generatedKeysHolder) {
                verify(s3MovieVideoRepository).generatePutPresignedUrl(key);
            }
            assertEquals(manifestKey, movie.getVideoManifestS3Key());
            assertEquals(generatedKeysHolder, new java.util.ArrayList<>(movie.getVideoFragments().keySet()));
            assertEquals(java.util.List.of("10.0", "12.5"),
                    new java.util.ArrayList<>(movie.getVideoFragments().values()));

            verify(movieRepository).save(movie);
        }

        @Test
        @DisplayName("Should delete old video data before generating new URLs")
        void shouldDeleteOldVideoDataBeforeGeneratingNewUrls() {
            // given
            UUID movieId = UUID.randomUUID();
            String movieIdStr = movieId.toString();

            Movie movie = new Movie();
            movie.setId(movieId);
            movie.setVideoManifestS3Key("old/index.m3u8");
            movie.setVideoFragments(new LinkedHashMap<>(Map.of("old/0.ts", "10.0")));

            MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
            request.setManifestContent("#EXTM3U\n#EXT-X-ENDLIST\n");

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(s3MovieVideoRepository.generateManifestKey(movieIdStr)).thenReturn("new/index.m3u8");
            when(s3MovieVideoRepository.generateFragmentKeys(eq(movieIdStr), anyInt()))
                    .thenAnswer(inv -> java.util.Collections.<String>emptyList());
            when(s3MovieVideoRepository.generatePutPresignedUrl(any()))
                    .thenAnswer(inv -> "https://presign-put/" + inv.getArgument(0, String.class));

            // when
            movieService.createMovieUploadUrls(movieIdStr, request);

            verify(s3MovieVideoRepository).deleteObject("old/index.m3u8");
            verify(s3MovieVideoRepository).deleteObject("old/0.ts");

            assertEquals("new/index.m3u8", movie.getVideoManifestS3Key());
            assertTrue(movie.getVideoFragments().isEmpty());

            verify(movieRepository, times(2)).save(movie);
        }

        @Test
        @DisplayName("Should throw NotFoundException when movie not found")
        void shouldThrowNotFoundWhenMovieNotFound() {
            // given
            UUID movieId = UUID.randomUUID();
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            // then
            assertThrows(NotFoundException.class,
                    () -> movieService.createMovieUploadUrls(movieId.toString(), new MovieUploadUrlsRequest()));
        }
    }
}
