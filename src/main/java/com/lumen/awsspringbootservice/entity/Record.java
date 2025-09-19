package com.lumen.awsspringbootservice.entity;


import com.lumen.awsspringbootservice.enums.PlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "records")
public class Record {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID purchaseId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private UUID movieId;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    private BigDecimal price;
    private LocalDateTime purchasedAt;
}
