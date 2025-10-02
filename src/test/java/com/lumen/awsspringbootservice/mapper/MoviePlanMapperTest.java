package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.enums.PlanType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                IdMapperImpl.class,
                MoviePlanMapperImpl.class
        },
        properties = {
                "spring.profiles.active=test"
        })
@DisplayName("MoviePlanMapper Unit Tests")
class MoviePlanMapperTest extends BaseMapperTest {

    @Autowired
    private MoviePlanMapper moviePlanMapper;

    @Nested
    @DisplayName("DTO to Entity Mapping")
    class DtoToEntityMapping {

        @Test
        @DisplayName("Should map MoviePlanShortDto to MoviePlan entity")
        void shouldMapDtoToEntity() {
            // given
            MoviePlanShortDto dto = buildMoviePlanShortDto(PlanType.MONTH);

            // when
            MoviePlan entity = moviePlanMapper.toEntity(dto);

            // then
            assertNotNull(entity);
            assertEquals(dto.getId(), entity.getId().toString());
            assertEquals(dto.getType(), entity.getType());
            assertEquals(dto.getPrice(), entity.getPrice());
        }

        @Test
        @DisplayName("Should return null when mapping null dto")
        void shouldReturnNullWhenDtoIsNull() {
            // when
            MoviePlan entity = moviePlanMapper.toEntity(null);

            // then
            assertNull(entity);
        }
    }

    @Nested
    @DisplayName("Entity to DTO Mapping")
    class EntityToDtoMapping {

        @Test
        @DisplayName("Should map MoviePlan entity to MoviePlanShortDto")
        void shouldMapEntityToDto() {
            // given
            MoviePlan entity = buildMoviePlanEntity(null, PlanType.WEEK);
            entity.setPrice(new BigDecimal("14.99"));

            // when
            MoviePlanShortDto dto = moviePlanMapper.toDto(entity);

            // then
            assertNotNull(dto);
            assertEquals(entity.getId().toString(), dto.getId());
            assertEquals(entity.getType(), dto.getType());
            assertEquals(entity.getPrice(), dto.getPrice());
        }

        @Test
        @DisplayName("Should return null when mapping null entity")
        void shouldReturnNullWhenEntityIsNull() {
            // when
            MoviePlanShortDto dto = moviePlanMapper.toDto(null);

            // then
            assertNull(dto);
        }
    }
}