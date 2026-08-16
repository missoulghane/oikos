package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.SetMeetingQuorumSettingCommand;
import com.architek.oikos.meeting.application.dto.MeetingQuorumSettingView;
import com.architek.oikos.meeting.application.port.in.SetMeetingQuorumSettingUseCase;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.repository.MeetingQuorumSettingRepository;
import com.architek.oikos.meeting.domain.valueobject.MeetingQuorumSettingId;

/**
 * Upsert by (property, meetingType) - the pair is unique in base, and a
 * syndic setting the ordinary quorum for the second time means "change it",
 * not "add another one".
 *
 * <p>Changing a threshold never touches meetings already created: they carry
 * their own snapshot (ADR 0002 §5).
 */
@Component
public class SetMeetingQuorumSettingService implements SetMeetingQuorumSettingUseCase {

    private final MeetingQuorumSettingRepository quorumSettingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public SetMeetingQuorumSettingService(MeetingQuorumSettingRepository quorumSettingRepository,
                                           PropertyDirectoryPort propertyDirectoryPort) {
        this.quorumSettingRepository = quorumSettingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
    }

    @Override
    @Transactional
    public MeetingQuorumSettingView set(SetMeetingQuorumSettingCommand command) {
        propertyDirectoryPort.getProperty(command.propertyId());

        MeetingQuorumSetting setting = quorumSettingRepository
                .findByPropertyAndType(command.propertyId(), command.meetingType())
                .map(existing -> existing.withQuorumPercentage(command.quorumPercentage()))
                .orElseGet(() -> MeetingQuorumSetting.create(MeetingQuorumSettingId.newId(), command.propertyId(),
                        command.meetingType(), command.quorumPercentage()));

        return MeetingQuorumSettingView.from(quorumSettingRepository.save(setting));
    }
}
