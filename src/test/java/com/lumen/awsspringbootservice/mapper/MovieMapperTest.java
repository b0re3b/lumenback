package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.PageResponse;
import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.dto.request.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.response.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.MovieResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.enums.PlanType;
import com.lumen.awsspringbootservice.repository.S3MoviePosterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = {
                IdMapperImpl.class,
                MoviePlanMapperImpl.class,
                MovieMapperImpl.class,
                S3UrlMapper.class
        },
        properties = {"spring.profiles.active=test"}
)
@DisplayName("MovieMapper Unit Tests")
class MovieMapperTest extends BaseMapperTest {

    @MockitoBean
    private S3MoviePosterRepository s3MoviePosterRepository;

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
    }

    @Nested
    @DisplayName("DTO to Response Mapping")
    class DtoToResponseMapping {
        @Test
        @DisplayName("Should map MovieDto to MovieResponse")
        void shouldMapDtoToMovieResponse() {
            MovieDto dto = buildMovieDto();

            MovieResponse response = movieMapper.toResponse(dto);

            assertNotNull(response);
            assertEquals(dto.getTitle(), response.getTitle());
            assertEquals(dto.getId(), response.getId());
        }

        @Test
        @DisplayName("Should map MovieDto to MovieDetailsResponse including MoviePlans")
        void shouldMapDtoToMovieDetailsResponse() {
            MovieDto dto = buildMovieDto();

            MovieDetailsResponse response = movieMapper.toDetailsResponse(dto);

            assertNotNull(response);
            assertEquals(dto.getTitle(), response.getTitle());
            assertNotNull(response.getMoviePlans());
            assertEquals(dto.getMoviePlanShortDtoList().size(), response.getMoviePlans().size());
        }
    }

    @Nested
    @DisplayName("Page Mapping")
    class PageMapping {
        @Test
        @DisplayName("Should map Page<MovieResponse> to PageResponse<MovieResponse>")
        void shouldMapPageToPageResponse() {
            MovieDto dto = buildMovieDto();
            MovieResponse response = movieMapper.toResponse(dto);
            Page<MovieResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 5), 1);

            PageResponse<MovieResponse> pageResponse = movieMapper.toPageResponse(page);

            assertNotNull(pageResponse);
            assertEquals(1, pageResponse.getPage());
            assertEquals(5, pageResponse.getSize());
            assertEquals(1, pageResponse.getContent().size());
        }
    }

    @Nested
    @DisplayName("MovieCreationRequest Mapping")
    class CreationRequestMapping {
        @Test
        @DisplayName("Should map MovieCreationRequest to MovieDto")
        void shouldMapRequestToDto() {
            MovieCreationRequest request = buildMovieCreationRequest(PlanType.MONTH);

            MovieDto dto = movieMapper.toDto(request);

            assertNotNull(dto);
            assertEquals(request.getTitle(), dto.getTitle());
            assertEquals(request.getAuthor(), dto.getAuthor());
            assertEquals(request.getDescription(), dto.getDescription());
        }

        @Test
        @DisplayName("Should map MovieCreationRequest to Movie entity including MoviePlans")
        void shouldMapRequestToEntity() {
            MovieCreationRequest request = buildMovieCreationRequest(PlanType.MONTH);

            Movie entity = movieMapper.toEntity(request);

            assertNotNull(entity);
            assertEquals(request.getTitle(), entity.getTitle());
            assertEquals(request.getAuthor(), entity.getAuthor());
            assertNotNull(entity.getMoviePlans());
            assertEquals(request.getMoviePlanShortDtoList().size(), entity.getMoviePlans().size());

            for (MoviePlan plan : entity.getMoviePlans()) {
                assertNotNull(plan.getMovie());
                assertEquals(entity, plan.getMovie());
            }
        }
    }
}