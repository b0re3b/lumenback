package com.lumen.awsspringbootservice.mapper;

import com.lumen.awsspringbootservice.dto.response.PageResponse;
import org.springframework.data.domain.Page;

public interface PageMapper {

    default <T> PageResponse<T> toPageResponse(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setPage(page.getNumber() + 1);
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }
}

