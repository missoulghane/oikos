package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConvocationChannelJpaRepository extends JpaRepository<ConvocationChannelEntity, String> {

    List<ConvocationChannelEntity> findByActiveTrueOrderByPositionAsc();

    List<ConvocationChannelEntity> findAllByOrderByPositionAsc();
}
