package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.DeleteGeneralMeetingCommand;
import com.architek.oikos.meeting.application.port.in.DeleteGeneralMeetingUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.MeetingNotDeletableException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;

/**
 * Only a meeting nobody has been convoked to can be deleted.
 *
 * <p>This is the one restriction kept when the editing rules were relaxed (ADR
 * 0002 §8), and it is not the same kind of rule: editing a convoked meeting
 * corrects it, deleting one destroys its convocations, its votes and its
 * minutes through the database's ON DELETE CASCADE. Cancelling an assembly
 * that has been announced is a decision to be minuted, not a row to remove.
 */
@Component
public class DeleteGeneralMeetingService implements DeleteGeneralMeetingUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;

    public DeleteGeneralMeetingService(GeneralMeetingRepository generalMeetingRepository) {
        this.generalMeetingRepository = generalMeetingRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteGeneralMeetingCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.id())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.id()));
        if (meeting.getStatus() != MeetingStatus.DRAFT && meeting.getStatus() != MeetingStatus.SCHEDULED) {
            throw new MeetingNotDeletableException(meeting.getStatus());
        }
        generalMeetingRepository.deleteById(meeting.getId());
    }
}
