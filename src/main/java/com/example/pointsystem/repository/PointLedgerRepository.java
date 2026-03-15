package com.example.pointsystem.repository;

import com.example.pointsystem.domain.PointLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointLedgerRepository extends JpaRepository<PointLedger, Long> {
    Optional<PointLedger> findByPointKey(String pointKey);
}
