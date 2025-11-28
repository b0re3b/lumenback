package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.purchase.PurchaseDto;
import com.lumen.awsspringbootservice.entity.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {MovieMapper.class, IdMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PurchaseMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "movieDto", source = "movie")
    @Mapping(target = "selectedPlanType", source = "selectedMoviePlan.type")
    PurchaseDto toDto(Purchase purchase);
}