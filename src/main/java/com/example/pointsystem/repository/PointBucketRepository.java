package com.example.pointsystem.repository;

import com.example.pointsystem.domain.PointBucket;
import com.example.pointsystem.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointBucketRepository extends JpaRepository<PointBucket, Long> {
    Optional<PointBucket> findByPointKey(String pointKey);
    List<PointBucket> findByUser(User user);
}
