package com.architek.oikos.meeting.web.controller;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.meeting.application.command.CloseGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.CreateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.DeleteGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.OpenGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.ScheduleGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommand;
import com.architek.oikos.meeting.application.command.UpdateGeneralMeetingCommentCommand;
import com.architek.oikos.meeting.application.port.in.CloseGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.CreateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.DeleteGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.GetGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ListGeneralMeetingsByPropertyUseCase;
import com.architek.oikos.meeting.application.port.in.OpenGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ScheduleGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateGeneralMeetingCommentUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateGeneralMeetingUseCase;
import com.architek.oikos.meeting.application.query.GetGeneralMeetingQuery;
import com.architek.oikos.meeting.application.query.ListGeneralMeetingsByPropertyQuery;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.VenueType;
import com.architek.oikos.meeting.web.request.CreateGeneralMeetingRequest;
import com.architek.oikos.meeting.web.request.OpenGeneralMeetingRequest;
import com.architek.oikos.meeting.web.request.ScheduleGeneralMeetingRequest;
import com.architek.oikos.meeting.web.request.UpdateGeneralMeetingCommentRequest;
import com.architek.oikos.meeting.web.request.UpdateGeneralMeetingRequest;
import com.architek.oikos.meeting.web.response.GeneralMeetingReferenceResponse;
import com.architek.oikos.meeting.web.response.GeneralMeetingResponse;
import com.architek.oikos.meeting.web.response.PagedGeneralMeetingResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Reads are gated by meeting:read (which every owner holds), writes by
 * meeting:manage (board and manager tiers only). The endpoints scoped by
 * meeting id resolve their property through GetGeneralMeetingUseCase inside
 * PropertyAccessEvaluator, never through a repository (rule 6).
 *
 * <p>Convening is not a transition of its own here: it happens as a
 * consequence of generating the convocations (see ConvocationController),
 * because a meeting is convened by convoking people, not by declaring it so.
 * Publishing the minutes, the last transition, arrives with lot 5.
 */
@RestController
public class GeneralMeetingController {

    private final CreateGeneralMeetingUseCase createGeneralMeetingUseCase;
    private final UpdateGeneralMeetingUseCase updateGeneralMeetingUseCase;
    private final UpdateGeneralMeetingCommentUseCase updateGeneralMeetingCommentUseCase;
    private final ScheduleGeneralMeetingUseCase scheduleGeneralMeetingUseCase;
    private final DeleteGeneralMeetingUseCase deleteGeneralMeetingUseCase;
    private final GetGeneralMeetingUseCase getGeneralMeetingUseCase;
    private final ListGeneralMeetingsByPropertyUseCase listGeneralMeetingsByPropertyUseCase;
    private final OpenGeneralMeetingUseCase openGeneralMeetingUseCase;
    private final CloseGeneralMeetingUseCase closeGeneralMeetingUseCase;

    public GeneralMeetingController(CreateGeneralMeetingUseCase createGeneralMeetingUseCase,
                                     UpdateGeneralMeetingUseCase updateGeneralMeetingUseCase,
                                     UpdateGeneralMeetingCommentUseCase updateGeneralMeetingCommentUseCase,
                                     ScheduleGeneralMeetingUseCase scheduleGeneralMeetingUseCase,
                                     DeleteGeneralMeetingUseCase deleteGeneralMeetingUseCase,
                                     GetGeneralMeetingUseCase getGeneralMeetingUseCase,
                                     ListGeneralMeetingsByPropertyUseCase listGeneralMeetingsByPropertyUseCase,
                                     OpenGeneralMeetingUseCase openGeneralMeetingUseCase,
                                     CloseGeneralMeetingUseCase closeGeneralMeetingUseCase) {
        this.createGeneralMeetingUseCase = createGeneralMeetingUseCase;
        this.updateGeneralMeetingUseCase = updateGeneralMeetingUseCase;
        this.updateGeneralMeetingCommentUseCase = updateGeneralMeetingCommentUseCase;
        this.scheduleGeneralMeetingUseCase = scheduleGeneralMeetingUseCase;
        this.deleteGeneralMeetingUseCase = deleteGeneralMeetingUseCase;
        this.getGeneralMeetingUseCase = getGeneralMeetingUseCase;
        this.listGeneralMeetingsByPropertyUseCase = listGeneralMeetingsByPropertyUseCase;
        this.openGeneralMeetingUseCase = openGeneralMeetingUseCase;
        this.closeGeneralMeetingUseCase = closeGeneralMeetingUseCase;
    }

    @PreAuthorize("@propertyAccess.canManageMeetings(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/general-meetings")
    public ResponseEntity<GeneralMeetingReferenceResponse> create(@PathVariable String propertyId,
                                                                    @Valid @RequestBody CreateGeneralMeetingRequest request) {
        GeneralMeetingId id = createGeneralMeetingUseCase.create(new CreateGeneralMeetingCommand(
                EntityId.of(propertyId), request.meetingType(), request.title(), request.scheduledAt(),
                venueOrNull(request.venueType(), request.venueAddress(), request.venueLink())));
        return ResponseEntity.status(HttpStatus.CREATED).body(GeneralMeetingReferenceResponse.from(id));
    }

    @PreAuthorize("@propertyAccess.canReadMeetings(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/general-meetings")
    public PagedGeneralMeetingResponse listByProperty(@PathVariable String propertyId,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(required = false) String type) {
        return PagedGeneralMeetingResponse.from(listGeneralMeetingsByPropertyUseCase.listGeneralMeetings(
                new ListGeneralMeetingsByPropertyQuery(EntityId.of(propertyId), parseStatus(status), parseType(type),
                        PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canReadMeeting(authentication, #id)")
    @GetMapping("/general-meetings/{id}")
    public GeneralMeetingResponse get(@PathVariable String id) {
        return GeneralMeetingResponse.from(
                getGeneralMeetingUseCase.getGeneralMeeting(new GetGeneralMeetingQuery(GeneralMeetingId.of(id))));
    }

    /**
     * The syndic's note of intent, on its own endpoint rather than as one more
     * field above: it is edited on its own screen, and going through the full
     * update would make saving a comment capable of overwriting the date and the
     * venue with whatever that screen happened to be holding.
     */
    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @PutMapping("/general-meetings/{id}/comment")
    public GeneralMeetingResponse updateComment(@PathVariable String id,
                                                  @Valid @RequestBody UpdateGeneralMeetingCommentRequest request) {
        return GeneralMeetingResponse.from(updateGeneralMeetingCommentUseCase.updateComment(
                new UpdateGeneralMeetingCommentCommand(GeneralMeetingId.of(id), request.comment())));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @PutMapping("/general-meetings/{id}")
    public GeneralMeetingResponse update(@PathVariable String id,
                                           @Valid @RequestBody UpdateGeneralMeetingRequest request) {
        return GeneralMeetingResponse.from(updateGeneralMeetingUseCase.update(new UpdateGeneralMeetingCommand(
                GeneralMeetingId.of(id), request.meetingType(), request.title(), request.scheduledAt(),
                venueOrNull(request.venueType(), request.venueAddress(), request.venueLink()))));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @PostMapping("/general-meetings/{id}/schedule")
    public GeneralMeetingResponse schedule(@PathVariable String id,
                                             @Valid @RequestBody ScheduleGeneralMeetingRequest request) {
        return GeneralMeetingResponse.from(scheduleGeneralMeetingUseCase.schedule(new ScheduleGeneralMeetingCommand(
                GeneralMeetingId.of(id), request.scheduledAt(),
                new MeetingVenue(request.venueType(), request.venueAddress(), request.venueLink()))));
    }

    /**
     * Opens the session. Refused if the quorum is not reached, unless the body
     * says forceWithoutQuorum - a lawful decision that is then recorded on the
     * meeting and printed in its minutes (ADR 0002 §5).
     */
    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @PostMapping("/general-meetings/{id}/open")
    public GeneralMeetingResponse open(@PathVariable String id,
                                         @RequestBody(required = false) OpenGeneralMeetingRequest request) {
        boolean force = request != null && request.forceWithoutQuorum();
        return GeneralMeetingResponse.from(openGeneralMeetingUseCase.open(new OpenGeneralMeetingCommand(
                GeneralMeetingId.of(id), force)));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @PostMapping("/general-meetings/{id}/close")
    public GeneralMeetingResponse close(@PathVariable String id) {
        return GeneralMeetingResponse.from(
                closeGeneralMeetingUseCase.close(new CloseGeneralMeetingCommand(GeneralMeetingId.of(id))));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #id)")
    @DeleteMapping("/general-meetings/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteGeneralMeetingUseCase.delete(new DeleteGeneralMeetingCommand(GeneralMeetingId.of(id)));
        return ResponseEntity.noContent().build();
    }

    /** A draft may legitimately have no venue yet, so an absent venueType is not an error here. */
    private static MeetingVenue venueOrNull(VenueType type, String address, String link) {
        return type == null ? null : new MeetingVenue(type, address, link);
    }

    private static MeetingStatus parseStatus(String status) {
        return status == null || status.isBlank() ? null : MeetingStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }

    private static MeetingType parseType(String type) {
        return type == null || type.isBlank() ? null : MeetingType.valueOf(type.toUpperCase(Locale.ROOT));
    }
}
