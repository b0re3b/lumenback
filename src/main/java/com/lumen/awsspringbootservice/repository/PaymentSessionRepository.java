package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.PaymentSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentSessionRepository extends JpaRepository<PaymentSession, UUID> {
}
