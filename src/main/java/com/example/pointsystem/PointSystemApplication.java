package com.example.pointsystem;

import com.example.pointsystem.domain.PointPolicy;
import com.example.pointsystem.repository.PointPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class PointSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner initPolicy(PointPolicyRepository pointPolicyRepository) {
        return args -> {
            if (pointPolicyRepository.count() == 0) {
                pointPolicyRepository.save(
                        PointPolicy.createDefault(100000, 1000000, 365)
                );
            }
        };
    }
}