package com.lumen.awsspringbootservice.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.UUID;


@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface IdMapper {
    default UUID map(String id) {
        return id != null ? UUID.fromString(id) : null;
    }

    default String map(UUID id) {
        return id != null ? id.toString() : null;
    }
}

