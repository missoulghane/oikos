package com.architek.oikos.user.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OnboardingLeadJpaRepository extends JpaRepository<OnboardingLeadEntity, UUID> {

    Optional<OnboardingLeadEntity> findByEmail(String email);
}
