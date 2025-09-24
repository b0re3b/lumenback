package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.movie.MoviePlanShortDto;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {IdMapper.class})
public interface MoviePlanMapper {

    MoviePlan toEntity(MoviePlanShortDto dto);

    MoviePlanShortDto toDto(MoviePlan entity);
}
