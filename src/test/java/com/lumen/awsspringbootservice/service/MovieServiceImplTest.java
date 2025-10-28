package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.request.MovieFiltersRequest;
import com.lumen.awsspringbootservice.dto.request.MovieUploadUrlsRequest;
import com.lumen.awsspringbootservice.dto.response.MovieUploadUrlsResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
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
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
        @DisplayName("Should build full specification and return filtered movies")
        void shouldBuildFullSpecificationAndReturnMovies() {
            // given
            MovieFiltersRequest filter = new MovieFiltersRequest();
            filter.setTitle("Matrix");
            filter.setAuthor("Wachowski");
            filter.setGenres(List.of(Genre.ACTION, Genre.COMEDY));
            filter.setPlanTypes(List.of(PlanType.MONTH, PlanType.WEEK));
            filter.setMinRating(4.5);
            filter.setPremiereDateFrom(LocalDate.of(1999, 1, 1));
            filter.setPremiereDateTo(LocalDate.of(2003, 12, 31));

            Movie entity = new Movie();
            entity.setId(UUID.randomUUID());
            MovieDto dto = new MovieDto();
            dto.setId(entity.getId().toString());

            PageRequest pageable = PageRequest.of(0, 10);
            Page<Movie> entitiesPage = new PageImpl<>(List.of(entity));

            when(movieRepository.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(entitiesPage);
            when(movieMapper.toDto(entity)).thenReturn(dto);

            // when
            Page<MovieDto> result = movieService.getMovies(filter, pageable.getPageNumber(), pageable.getPageSize());

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(dto.getId(), result.getContent().get(0).getId());

            verify(movieRepository).findAll(any(Specification.class), eq(pageable));
            verify(movieMapper).toDto(entity);

            verify(movieRepository).findAll(any(Specification.class), eq(pageable));
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
        @DisplayName("Should generate new upload URLs and persist updated fragments")
        void shouldGenerateNewUploadUrlsAndPersistMovie() {
            // given
            UUID movieId = UUID.randomUUID();
            Movie movie = new Movie();
            movie.setId(movieId);

            MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
            request.setManifestContent("#EXTM3U\n#EXTINF:8.0,\n0.ts\n#EXTINF:9.5,\n1.ts\n#EXT-X-ENDLIST");

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(s3MovieVideoRepository.generateManifestKey(movieId.toString())).thenReturn("folder/" + movieId + "/index.m3u8");
            when(s3MovieVideoRepository.generateFragmentKeys(movieId.toString(), 2))
                    .thenReturn(List.of("folder/" + movieId + "/0.ts", "folder/" + movieId + "/1.ts"));
            when(s3MovieVideoRepository.generatePutPresignedUrl(anyString()))
                    .thenAnswer(invocation -> "https://s3.local/" + invocation.getArgument(0));

            // when
            MovieUploadUrlsResponse response = movieService.createMovieUploadUrls(movieId.toString(), request);

            // then
            assertNotNull(response);
            assertEquals(movieId.toString(), response.getMovieId());
            assertTrue(response.getMainManifestUrl().contains("index.m3u8"));
            assertEquals(2, response.getSegmentsUrls().size());

            verify(s3MovieVideoRepository).generateManifestKey(movieId.toString());
            verify(s3MovieVideoRepository, times(3)).generatePutPresignedUrl(anyString());
            verify(movieRepository, atLeastOnce()).save(movie);

            // Ensure fragments were persisted
            assertNotNull(movie.getVideoFragments());
            assertEquals(2, movie.getVideoFragments().size());
            assertEquals("folder/" + movieId + "/0.ts", movie.getVideoFragments().get(0).getVideoFragmentS3Key());
            assertEquals("8.0", movie.getVideoFragments().get(0).getDuration());
            assertEquals("9.5", movie.getVideoFragments().get(1).getDuration());
        }

        @Test
        @DisplayName("Should delete old S3 video data before generating new URLs")
        void shouldDeleteOldVideoDataBeforeGeneratingNewUrls() {
            // given
            UUID movieId = UUID.randomUUID();
            Movie movie = new Movie();
            movie.setId(movieId);
            movie.setVideoManifestS3Key("old/index.m3u8");

            Movie.VideoFragment old1 = new Movie.VideoFragment();
            old1.setVideoFragmentS3Key("old/0.ts");
            old1.setDuration("5.0");

            Movie.VideoFragment old2 = new Movie.VideoFragment();
            old2.setVideoFragmentS3Key("old/1.ts");
            old2.setDuration("7.0");

            movie.setVideoFragments(new ArrayList<>(List.of(old1, old2)));

            MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
            request.setManifestContent("#EXTM3U\n#EXTINF:10.0,\n0.ts\n#EXT-X-ENDLIST");

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(s3MovieVideoRepository.generateManifestKey(movieId.toString())).thenReturn("new/index.m3u8");
            when(s3MovieVideoRepository.generateFragmentKeys(movieId.toString(), 1))
                    .thenReturn(List.of("new/0.ts"));
            when(s3MovieVideoRepository.generatePutPresignedUrl(anyString()))
                    .thenAnswer(invocation -> "https://s3.local/" + invocation.getArgument(0));

            // when
            movieService.createMovieUploadUrls(movieId.toString(), request);

            // then
            verify(s3MovieVideoRepository).deleteObject("old/index.m3u8");
            verify(s3MovieVideoRepository).deleteObject("old/0.ts");
            verify(s3MovieVideoRepository).deleteObject("old/1.ts");
            verify(movieRepository, atLeastOnce()).save(movie);
        }

        @Test
        @DisplayName("Should throw NotFoundException if movie not found")
        void shouldThrowNotFoundWhenMovieMissing() {
            UUID movieId = UUID.randomUUID();
            MovieUploadUrlsRequest request = new MovieUploadUrlsRequest();
            request.setManifestContent("#EXTM3U\n#EXT-X-ENDLIST");

            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> movieService.createMovieUploadUrls(movieId.toString(), request));
        }
    }

    @Nested
    @DisplayName("getMovieVideoUrl Tests")
    class GetMovieVideoUrlTests {

        @Test
        @DisplayName("Should generate manifest with presigned GET URLs for all fragments")
        void shouldGenerateManifestWithPresignedGetUrls() {
            // given
            UUID movieId = UUID.randomUUID();
            Movie movie = new Movie();
            movie.setId(movieId);

            Movie.VideoFragment fragment1 = new Movie.VideoFragment();
            fragment1.setVideoFragmentS3Key("video/0.ts");
            fragment1.setDuration("8.0");

            Movie.VideoFragment fragment2 = new Movie.VideoFragment();
            fragment2.setVideoFragmentS3Key("video/1.ts");
            fragment2.setDuration("9.5");

            movie.setVideoFragments(List.of(fragment1, fragment2));

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));
            when(s3MovieVideoRepository.generateGetPresignedUrl("video/0.ts"))
                    .thenReturn("https://s3.local/video/0.ts?token");
            when(s3MovieVideoRepository.generateGetPresignedUrl("video/1.ts"))
                    .thenReturn("https://s3.local/video/1.ts?token");

            // when
            String manifest = movieService.getMovieVideoUrl(movieId.toString());

            // then
            assertNotNull(manifest);
            assertTrue(manifest.contains("#EXTM3U"));
            assertTrue(manifest.contains("https://s3.local/video/0.ts?token"));
            assertTrue(manifest.contains("https://s3.local/video/1.ts?token"));
            assertTrue(manifest.contains("#EXT-X-ENDLIST"));

            verify(s3MovieVideoRepository, times(2)).generateGetPresignedUrl(anyString());
            verify(movieRepository).findById(movieId);
        }

        @Test
        @DisplayName("Should throw IllegalStateException if movie has no fragments")
        void shouldThrowIfMovieHasNoFragments() {
            UUID movieId = UUID.randomUUID();
            Movie movie = new Movie();
            movie.setId(movieId);
            movie.setVideoFragments(Collections.emptyList());

            when(movieRepository.findById(movieId)).thenReturn(Optional.of(movie));

            assertThrows(IllegalStateException.class,
                    () -> movieService.getMovieVideoUrl(movieId.toString()));
        }

        @Test
        @DisplayName("Should throw NotFoundException if movie does not exist")
        void shouldThrowIfMovieNotFound() {
            UUID movieId = UUID.randomUUID();
            when(movieRepository.findById(movieId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class,
                    () -> movieService.getMovieVideoUrl(movieId.toString()));
        }
    }

}
