package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MovieDto;
import com.lumen.awsspringbootservice.entity.Movie;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {MoviePlanMapper.class, IdMapper.class})
public interface MovieMapper {

    @Mapping(target = "moviePlans", source = "moviePlanShortDtoList")
    Movie toEntity(MovieDto dto);

    @Mapping(target = "moviePlanShortDtoList", source = "moviePlans")
    MovieDto toDto(Movie entity);

    List<Movie> toEntityList(List<MovieDto> dtoList);

    List<MovieDto> toDtoList(List<Movie> entityList);

    @AfterMapping
    default void linkMoviePlans(@MappingTarget Movie movie) {
        if (movie.getMoviePlans() != null) {
            movie.getMoviePlans().forEach(plan -> plan.setMovie(movie));
        }
    }
}
