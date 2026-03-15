package com.example.pointsystem.repository;

import com.example.pointsystem.domain.PointLedger;
import com.example.pointsystem.domain.PointUsageAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointUsageAllocationRepository extends JpaRepository<PointUsageAllocation, Long> {
    List<PointUsageAllocation> findByUseLedgerOrderByIdAsc(PointLedger useLedger);
}