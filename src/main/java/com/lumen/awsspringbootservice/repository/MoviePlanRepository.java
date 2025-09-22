package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.MoviePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MoviePlanRepository extends JpaRepository<MoviePlan, UUID> {
}
