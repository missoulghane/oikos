package com.architek.oikos.meeting.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommentCommand;
import com.architek.oikos.meeting.application.dto.GeneralMeetingView;
import com.architek.oikos.meeting.application.port.in.UpdateGeneralMeetingCommentUseCase;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;

/**
 * Saves the meeting's note of intent, and nothing else.
 *
 * <p>A verb of its own rather than a field on UpdateGeneralMeetingUseCase: the
 * comment is edited on its own screen, and routing it through the full update
 * would let "save a comment" overwrite the date and the venue with whatever
 * that screen was holding - a lost update on the two fields a convoked
 * copropriétaire relies on.
 */
@Component
public class UpdateGeneralMeetingCommentService implements UpdateGeneralMeetingCommentUseCase {

    private final GeneralMeetingRepository generalMeetingRepository;
    private final GeneralMeetingViewAssembler viewAssembler;

    public UpdateGeneralMeetingCommentService(GeneralMeetingRepository generalMeetingRepository,
                                               GeneralMeetingViewAssembler viewAssembler) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.viewAssembler = viewAssembler;
    }

    @Override
    @Transactional
    public GeneralMeetingView updateComment(UpdateGeneralMeetingCommentCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));
        return viewAssembler.toView(generalMeetingRepository.save(meeting.withComment(command.comment())));
    }
}
