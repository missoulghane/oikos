package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.UpdateGeneralMeetingUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

/**
 * Editing the meeting's own fields - type, title, date, venue - at any point of
 * its lifecycle (ADR 0002 §8: no functional blocking rule for the time being).
 *
 * <p>Changing the meeting type deliberately does NOT re-read the quorum
 * configured for the new type: the snapshot belongs to the meeting from the
 * moment it exists, and re-reading it here would reintroduce exactly the drift
 * the snapshot prevents. A syndic who needs the other type's quorum creates
 * the meeting under that type.
 */
@Component
public class UpdateGeneralMeetingService implements UpdateGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public UpdateGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository,
                                        GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional
    public GeneralMeetingView update(UpdateGeneralMeetingCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.id()));
        GeneralMeeting updated = meeting.update(command.meetingType(), command.title(), command.scheduledAt(),
                command.venue());
        return viewAssembler.toView(generalMeetingRepository.save(updated));
    }
}
