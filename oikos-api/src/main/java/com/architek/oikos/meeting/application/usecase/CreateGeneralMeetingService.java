package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.PropertyInfo;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.MeetingQuorumSetting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.MeetingQuorumSettingRepository;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.QuorumPercentage;

/**
 * Creates a meeting as a draft, taking the two snapshots the aggregate lives
 * on: the quorum configured for this kind of meeting, and the property's
 * voting weight mode.
 *
 * <p>Both are read here, once, and never again - re-reading them at counting
 * time would let a configuration change made months later restate how a
 * meeting already held was counted (ADR 0002 §3 and §5). A property with no
 * quorum configured for this type gets QuorumPercentage.none(), which is not
 * a legal default but the honest statement that none was set.
 */
@Component
public class CreateGeneralMeetingService implements CreateGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final MeetingQuorumSettingRepository quorumSettingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;

    public CreateGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                        MeetingQuorumSettingRepository quorumSettingRepository,
                                        PropertyDirectoryPort propertyDirectoryPort) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.quorumSettingRepository = quorumSettingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
    }

    @Override
    @Transactional
    public GeneralMeetingId create(CreateGeneralMeetingCommand command) {
        // Existence of the property is validated by the port itself (404 otherwise).
        PropertyInfo property = propertyDirectoryPort.getProperty(command.propertyId());

        QuorumPercentage quorum = quorumSettingRepository
                .findByPropertyAndType(command.propertyId(), command.meetingType())
                .map(MeetingQuorumSetting::getQuorumPercentage)
                .orElseGet(QuorumPercentage::none);

        GeneralMeeting draft = GeneralMeeting.createDraft(GeneralMeetingId.newId(), command.propertyId(),
                command.meetingType(), command.title(), command.scheduledAt(), command.venue(), quorum,
                property.votingWeightMode());

        return generalMeetingRepository.save(draft).getId();
    }
}
