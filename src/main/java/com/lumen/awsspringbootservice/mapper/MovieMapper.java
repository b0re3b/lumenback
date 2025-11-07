package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.dto.request.movie.MovieCreationRequest;
import com.lumen.awsspringbootservice.dto.response.movie.MovieDetailsResponse;
import com.lumen.awsspringbootservice.dto.response.movie.MovieResponse;
import com.lumen.awsspringbootservice.entity.Movie;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {MoviePlanMapper.class, IdMapper.class, S3UrlMapper.class})
public interface MovieMapper extends PageMapper {

    @Mapping(target = "moviePlans", source = "moviePlanShortDtoList")
    Movie toEntity(MovieDto dto);

    @Mapping(target = "moviePlanShortDtoList", source = "moviePlans")
    MovieDto toDto(Movie entity);

    @Mapping(target = "posterUrl", source = "posterS3Key", qualifiedBy = PosterUrlMapping.class)
    MovieResponse toResponse(MovieDto dto);

    @Mapping(target = "moviePlans", source = "moviePlanShortDtoList")
    @Mapping(target = "posterUrl", source = "posterS3Key", qualifiedBy = PosterUrlMapping.class)
    MovieDetailsResponse toDetailsResponse(MovieDto dto);

    MovieDto toDto(MovieCreationRequest request);

    @Mapping(target = "moviePlans", source = "moviePlanShortDtoList")
    Movie toEntity(MovieCreationRequest request);

    @AfterMapping
    default void linkMoviePlans(@MappingTarget Movie movie) {
        if (movie.getMoviePlans() != null) {
            movie.getMoviePlans().forEach(plan -> plan.setMovie(movie));
        }
    }
}
