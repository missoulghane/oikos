package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyMediumJpaRepository extends JpaRepository<ReplyMediumEntity, String> {

    List<ReplyMediumEntity> findByActiveTrueOrderByPositionAsc();

    List<ReplyMediumEntity> findAllByOrderByPositionAsc();
}
