package com.lumen.awsspringbootservice.service;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.exception.NotFoundException;
import com.lumen.awsspringbootservice.mapper.MovieMapper;
import com.lumen.awsspringbootservice.repository.MovieRepository;
import com.lumen.awsspringbootservice.service.impl.MovieServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieServiceImpl Unit Tests")
class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieMapper movieMapper;

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
        void shouldSaveAndReturnMovieDto() {
            // given
            MovieDto dto = new MovieDto();
            Movie entity = new Movie();
            Movie savedEntity = new Movie();
            MovieDto savedDto = new MovieDto();

            when(movieMapper.toEntity(dto)).thenReturn(entity);
            when(movieRepository.save(entity)).thenReturn(savedEntity);
            when(movieMapper.toDto(savedEntity)).thenReturn(savedDto);

            // when
            MovieDto result = movieService.createMovie(dto);

            // then
            assertNotNull(result);
            assertEquals(savedDto, result);
            verify(movieMapper).toEntity(dto);
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
}
