package com.lumen.awsspringbootservice.util;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.enums.Genre;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MovieSpecifications {

    public static Specification<Movie> title(String title) {
        return (root, query, criteriaBuilder) ->
                title == null ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Movie> genre(Genre genre) {
        return (root, query, criteriaBuilder) ->
                genre == null ? null : criteriaBuilder.isMember(genre, root.get("genres"));
    }

    public static Specification<Movie> premiereDateAfter(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("premiereDate"), from.atStartOfDay());
    }

    public static Specification<Movie> premiereDateBefore(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("premiereDate"), to.atTime(23, 59, 59));
    }
}
