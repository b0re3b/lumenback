package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Purchase;
import com.lumen.awsspringbootservice.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID>, JpaSpecificationExecutor<Purchase> {

    Optional<Purchase> findByUserIdAndSelectedMoviePlanId(UUID userId, UUID selectedMoviePlanId);


    List<Purchase> findAllByPurchasedAtBetween(LocalDateTime from, LocalDateTime to);

    @Query(
            value = """
                    SELECT p.movie.id
                    FROM Purchase p
                    GROUP BY p.movie.id
                    ORDER BY COUNT(p) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.movie.id)
                    FROM Purchase p
                    """
    )
    Page<UUID> findTopSales(Pageable pageable);

    @Query(
            value = """
                    SELECT g
                    FROM Purchase p
                    JOIN p.movie.genres g
                    GROUP BY g
                    ORDER BY COUNT(p) DESC
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT g)
                    FROM Purchase p
                    JOIN p.movie.genres g
                    """
    )
    Page<Genre> findTopGenresOnly(
            Pageable pageable);

    List<Purchase> findAllByUserIdAndMovieId(UUID userId, UUID movieId);

    List<Purchase> findAllByUserId(UUID userId);
}
