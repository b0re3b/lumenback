package com.lumen.awsspringbootservice.entity;

import com.lumen.awsspringbootservice.enums.PlanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "movie_plans",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_movieplan_movie_type", columnNames = {"movie_id", "type"})
        }
)
public class MoviePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
            name = "movie_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_movieplan_movie")
    )
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private PlanType type;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
}

