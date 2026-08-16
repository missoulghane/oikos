package com.architek.oikos.meeting.domain.repository;

import java.util.List;
import java.util.Optional;

import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface MeetingQuorumSettingRepository {

    MeetingQuorumSetting save(MeetingQuorumSetting setting);

    Optional<MeetingQuorumSetting> findByPropertyAndType(EntityId propertyId, MeetingType meetingType);

    List<MeetingQuorumSetting> findByProperty(EntityId propertyId);
}
