package com.architek.oikos.meeting.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.architek.oikos.meeting.domain.valueobject.MeetingType;

public interface MeetingQuorumSettingJpaRepository extends JpaRepository<MeetingQuorumSettingEntity, UUID> {

    Optional<MeetingQuorumSettingEntity> findByPropertyIdAndMeetingType(UUID propertyId, MeetingType meetingType);

    List<MeetingQuorumSettingEntity> findByPropertyId(UUID propertyId);
}
