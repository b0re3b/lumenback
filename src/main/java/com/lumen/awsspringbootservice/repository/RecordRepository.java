package com.lumen.awsspringbootservice.repository;

import com.lumen.awsspringbootservice.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RecordRepository extends JpaRepository<Record, UUID>, JpaSpecificationExecutor<Record> {
}
