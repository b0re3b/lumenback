package com.lumen.awsspringbootservice.entity;

import com.lumen.awsspringbootservice.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "purchases")
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private MoviePlan selectedMoviePlan;

    private LocalDateTime purchasedAt;
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus purchaseStatus;
}

