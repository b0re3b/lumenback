package com.lumen.awsspringbootservice.util;

import com.lumen.awsspringbootservice.entity.Movie;
import com.lumen.awsspringbootservice.entity.MoviePlan;
import com.lumen.awsspringbootservice.entity.Review;
import com.lumen.awsspringbootservice.enums.Genre;
import com.lumen.awsspringbootservice.enums.PlanType;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;

public class MovieSpecifications {

    public static Specification<Movie> title(String title) {
        return (root, query, criteriaBuilder) ->
                title == null
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + title.toLowerCase() + "%"
                );
    }

    public static Specification<Movie> author(String author) {
        return (root, query, criteriaBuilder) ->
                author == null
                        ? null
                        : criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        "%" + author.toLowerCase() + "%"
                );
    }

    public static Specification<Movie> genres(List<Genre> genreList) {
        return (root, query, criteriaBuilder) -> {
            if (genreList == null || genreList.isEmpty()) {
                return null;
            }

            return genreList.stream()
                    .map(genre -> criteriaBuilder.isMember(genre, root.get("genres")))
                    .reduce(criteriaBuilder::and)
                    .orElse(null);
        };
    }

    public static Specification<Movie> planTypes(List<PlanType> planTypeList) {
        return (root, query, criteriaBuilder) -> {
            if (planTypeList == null || planTypeList.isEmpty()) {
                return null;
            }

            Join<Movie, MoviePlan> planJoin = root.join("moviePlans");
            query.groupBy(root.get("id"));
            query.having(criteriaBuilder.equal(
                    criteriaBuilder.countDistinct(planJoin.get("type")),
                    planTypeList.size()
            ));

            return planJoin.get("type").in(planTypeList);
        };
    }

    public static Specification<Movie> minRating(Double minRating) {
        return (root, query, criteriaBuilder) -> {
            if (minRating == null) return null;

            Join<Movie, Review> reviewJoin = root.join("reviews", JoinType.LEFT);
            query.groupBy(root.get("id"));
            query.having(
                    criteriaBuilder.ge(
                            criteriaBuilder.coalesce(criteriaBuilder.avg(reviewJoin.get("star")), 0.0),
                            minRating
                    )
            );

            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Movie> premiereDateAfter(LocalDate premiereDateFrom) {
        return (root, query, criteriaBuilder) ->
                premiereDateFrom == null
                        ? null
                        : criteriaBuilder.greaterThanOrEqualTo(
                        root.get("premiereDate"),
                        premiereDateFrom.atStartOfDay()
                );
    }

    public static Specification<Movie> premiereDateBefore(LocalDate premiereDateTo) {
        return (root, query, criteriaBuilder) ->
                premiereDateTo == null
                        ? null
                        : criteriaBuilder.lessThanOrEqualTo(
                        root.get("premiereDate"),
                        premiereDateTo.atTime(23, 59, 59)
                );
    }
}
