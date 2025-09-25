package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

}
