package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                IdMapperImpl.class,
                MoviePlanMapperImpl.class,
                MovieMapperImpl.class
        },
        properties = {"spring.profiles.active=test"}
)
@DisplayName("MovieMapper Unit Tests")
class MovieMapperTest extends BaseMapperTest {

    @Autowired
    private MovieMapper movieMapper;

    @Nested
    @DisplayName("DTO to Entity Mapping")
    class DtoToEntityMapping {

        @Test
        @DisplayName("Should map MovieDto to Movie entity including MoviePlans")
        void shouldMapDtoToEntity() {
            // given
            MovieDto dto = buildMovieDto();

            // when
            Movie entity = movieMapper.toEntity(dto);

            // then
            assertNotNull(entity);
            assertEquals(dto.getTitle(), entity.getTitle());
            assertEquals(dto.getDescription(), entity.getDescription());
            assertEquals(dto.getGenres(), entity.getGenres());
            assertNotNull(entity.getMoviePlans());
            assertEquals(dto.getMoviePlanShortDtoList().size(), entity.getMoviePlans().size());

            // check linkMoviePlans worked
            for (MoviePlan plan : entity.getMoviePlans()) {
                assertNotNull(plan.getMovie());
                assertEquals(entity, plan.getMovie());
            }
        }

        @Test
        @DisplayName("Should return null when mapping null dto")
        void shouldReturnNullWhenDtoIsNull() {
            // when
            Movie entity = movieMapper.toEntity(null);

            // then
            assertNull(entity);
        }
    }

    @Nested
    @DisplayName("Entity to DTO Mapping")
    class EntityToDtoMapping {

        @Test
        @DisplayName("Should map Movie entity to MovieDto including MoviePlans")
        void shouldMapEntityToDto() {
            // given
            Movie entity = buildMovieEntity();

            // when
            MovieDto dto = movieMapper.toDto(entity);

            // then
            assertNotNull(dto);
            assertEquals(entity.getTitle(), dto.getTitle());
            assertEquals(entity.getDescription(), dto.getDescription());
            assertEquals(entity.getGenres(), dto.getGenres());
            assertNotNull(dto.getMoviePlanShortDtoList());
            assertEquals(entity.getMoviePlans().size(), dto.getMoviePlanShortDtoList().size());

            MoviePlanShortDto planDto = dto.getMoviePlanShortDtoList().get(0);
            MoviePlan plan = entity.getMoviePlans().get(0);

            assertEquals(plan.getType(), planDto.getType());
            assertEquals(plan.getPrice(), planDto.getPrice());
        }

        @Test
        @DisplayName("Should return null when mapping null entity")
        void shouldReturnNullWhenEntityIsNull() {
            // when
            MovieDto dto = movieMapper.toDto(null);

            // then
            assertNull(dto);
        }
    }
}