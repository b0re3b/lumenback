package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Optional<Purchase> findByUserIdAndSelectedMoviePlanId(UUID userId, UUID selectedMoviePlanId);
}
