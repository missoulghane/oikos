package com.architek.oikos.meeting.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.meeting.application.command.GenerateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.PublishMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.UpdateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.command.ValidateMeetingMinutesCommand;
import com.architek.oikos.meeting.application.port.in.GenerateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.GetMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.PublishMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.in.ValidateMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.query.GetMeetingMinutesQuery;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.web.request.UpdateMeetingMinutesRequest;
import com.architek.oikos.meeting.web.response.MeetingMinutesResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The procès-verbal of one meeting: draft, edit, validate, publish.
 *
 * <p>Validation and publication are gated by meeting:minutes:publish rather
 * than meeting:manage. Preparing an assembly and putting its record out to
 * every copropriétaire are not the same responsibility, and the second is
 * irreversible.
 *
 * <p>Reading is open to every member (meeting:read) - a copropriétaire has to
 * be able to read the record of an assembly they were convoked to, including
 * while it is still a draft: their own remarks are in it.
 */
@RestController
public class MeetingMinutesController {

    private final GenerateMeetingMinutesUseCase generateMeetingMinutesUseCase;
    private final GetMeetingMinutesUseCase getMeetingMinutesUseCase;
    private final UpdateMeetingMinutesUseCase updateMeetingMinutesUseCase;
    private final ValidateMeetingMinutesUseCase validateMeetingMinutesUseCase;
    private final PublishMeetingMinutesUseCase publishMeetingMinutesUseCase;

    public MeetingMinutesController(GenerateMeetingMinutesUseCase generateMeetingMinutesUseCase,
                                     GetMeetingMinutesUseCase getMeetingMinutesUseCase,
                                     UpdateMeetingMinutesUseCase updateMeetingMinutesUseCase,
                                     ValidateMeetingMinutesUseCase validateMeetingMinutesUseCase,
                                     PublishMeetingMinutesUseCase publishMeetingMinutesUseCase) {
        this.generateMeetingMinutesUseCase = generateMeetingMinutesUseCase;
        this.getMeetingMinutesUseCase = getMeetingMinutesUseCase;
        this.updateMeetingMinutesUseCase = updateMeetingMinutesUseCase;
        this.validateMeetingMinutesUseCase = validateMeetingMinutesUseCase;
        this.publishMeetingMinutesUseCase = publishMeetingMinutesUseCase;
    }

    /** Drafts from the session's data. Re-running regenerates and discards manual edits. */
    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/minutes")
    public ResponseEntity<MeetingMinutesResponse> generate(@PathVariable String meetingId) {
        MeetingMinutesResponse response = MeetingMinutesResponse.from(generateMeetingMinutesUseCase.generate(
                new GenerateMeetingMinutesCommand(GeneralMeetingId.of(meetingId))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.canReadMeeting(authentication, #meetingId)")
    @GetMapping("/general-meetings/{meetingId}/minutes")
    public MeetingMinutesResponse get(@PathVariable String meetingId) {
        return MeetingMinutesResponse.from(getMeetingMinutesUseCase.getMinutes(
                new GetMeetingMinutesQuery(GeneralMeetingId.of(meetingId))));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PutMapping("/general-meetings/{meetingId}/minutes")
    public MeetingMinutesResponse update(@PathVariable String meetingId,
                                           @Valid @RequestBody UpdateMeetingMinutesRequest request) {
        return MeetingMinutesResponse.from(updateMeetingMinutesUseCase.update(
                new UpdateMeetingMinutesCommand(GeneralMeetingId.of(meetingId), request.content())));
    }

    /** Freezes the text; nothing but publication may follow. */
    @PreAuthorize("@propertyAccess.canPublishMinutesOfMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/minutes/validate")
    public MeetingMinutesResponse validate(@PathVariable String meetingId) {
        return MeetingMinutesResponse.from(validateMeetingMinutesUseCase.validate(
                new ValidateMeetingMinutesCommand(GeneralMeetingId.of(meetingId))));
    }

    /** Renders the validated text to PDF, files it, and ends the meeting's cycle. */
    @PreAuthorize("@propertyAccess.canPublishMinutesOfMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/minutes/publish")
    public MeetingMinutesResponse publish(@PathVariable String meetingId, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return MeetingMinutesResponse.from(publishMeetingMinutesUseCase.publish(new PublishMeetingMinutesCommand(
                GeneralMeetingId.of(meetingId), EntityId.of(principal.getUserId()))));
    }
}
