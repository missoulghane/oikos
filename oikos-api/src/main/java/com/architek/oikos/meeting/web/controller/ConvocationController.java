package com.architek.oikos.meeting.web.controller;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.meeting.application.command.CheckInConvocationCommand;
import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.command.RecordConvocationDeliveryCommand;
import com.architek.oikos.meeting.application.command.RemindPendingConvocationsCommand;
import com.architek.oikos.meeting.application.command.ReplyToConvocationCommand;
import com.architek.oikos.meeting.application.command.SendConvocationCommand;
import com.architek.oikos.meeting.application.command.SendPendingConvocationsCommand;
import com.architek.oikos.meeting.application.port.in.CheckInConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.GetAttendanceSummaryUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationDocumentUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.ListConvocationsByMeetingUseCase;
import com.architek.oikos.meeting.application.port.in.ListMyConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.RecordConvocationDeliveryUseCase;
import com.architek.oikos.meeting.application.port.in.RemindPendingConvocationsUseCase;
import com.architek.oikos.meeting.application.port.in.ReplyToConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.SendConvocationUseCase;
import com.architek.oikos.meeting.application.port.in.SendPendingConvocationsUseCase;
import com.architek.oikos.meeting.application.query.GetAttendanceSummaryQuery;
import com.architek.oikos.meeting.application.query.GetConvocationQuery;
import com.architek.oikos.meeting.application.query.ListConvocationsByMeetingQuery;
import com.architek.oikos.meeting.application.query.ListMyConvocationsQuery;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.ConvocationStatus;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.web.request.CheckInConvocationRequest;
import com.architek.oikos.meeting.web.request.RecordConvocationDeliveryRequest;
import com.architek.oikos.meeting.web.request.ReplyToConvocationRequest;
import com.architek.oikos.meeting.web.request.SendConvocationRequest;
import com.architek.oikos.meeting.web.request.SendConvocationsRequest;
import com.architek.oikos.meeting.web.response.AttendanceSummaryResponse;
import com.architek.oikos.meeting.web.response.ConvocationResponse;
import com.architek.oikos.meeting.web.response.MyConvocationResponse;
import com.architek.oikos.meeting.web.response.ReminderResultResponse;
import com.architek.oikos.meeting.web.response.SendConvocationsResultResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Convocations: generation, sending, tracking, replies and check-in.
 *
 * <p>Generating and sending are two distinct actions (POST .../convocations vs
 * POST .../convocations/send). Generating creates one convocation per lot and
 * convokes the meeting; sending is what actually puts letters in the post. The
 * syndic generates, reviews the tracking table, and sends when ready.
 *
 * <p>Sending and recording a delivery are two further distinct verbs
 * (POST .../send vs PUT .../delivery-status). One performs an action, the
 * other asserts that a person performed one; a tracking table that cannot tell
 * them apart has lost the only thing it exists to record.
 *
 * <p>Replying is the one endpoint a plain copropriétaire may call, on their
 * own lot only - hence canReplyToConvocation rather than canManageConvocation.
 */
@RestController
public class ConvocationController {

    private final GenerateConvocationsUseCase generateConvocationsUseCase;
    private final SendConvocationUseCase sendConvocationUseCase;
    private final SendPendingConvocationsUseCase sendPendingConvocationsUseCase;
    private final RemindPendingConvocationsUseCase remindPendingConvocationsUseCase;
    private final RecordConvocationDeliveryUseCase recordConvocationDeliveryUseCase;
    private final ReplyToConvocationUseCase replyToConvocationUseCase;
    private final CheckInConvocationUseCase checkInConvocationUseCase;
    private final GetConvocationUseCase getConvocationUseCase;
    private final GetConvocationDocumentUseCase getConvocationDocumentUseCase;
    private final ListConvocationsByMeetingUseCase listConvocationsByMeetingUseCase;
    private final GetAttendanceSummaryUseCase getAttendanceSummaryUseCase;
    private final ListMyConvocationsUseCase listMyConvocationsUseCase;

    public ConvocationController(GenerateConvocationsUseCase generateConvocationsUseCase,
                                  SendConvocationUseCase sendConvocationUseCase,
                                  SendPendingConvocationsUseCase sendPendingConvocationsUseCase,
                                  RemindPendingConvocationsUseCase remindPendingConvocationsUseCase,
                                  RecordConvocationDeliveryUseCase recordConvocationDeliveryUseCase,
                                  ReplyToConvocationUseCase replyToConvocationUseCase,
                                  CheckInConvocationUseCase checkInConvocationUseCase,
                                  GetConvocationUseCase getConvocationUseCase,
                                  GetConvocationDocumentUseCase getConvocationDocumentUseCase,
                                  ListConvocationsByMeetingUseCase listConvocationsByMeetingUseCase,
                                  GetAttendanceSummaryUseCase getAttendanceSummaryUseCase,
                                  ListMyConvocationsUseCase listMyConvocationsUseCase) {
        this.generateConvocationsUseCase = generateConvocationsUseCase;
        this.sendConvocationUseCase = sendConvocationUseCase;
        this.sendPendingConvocationsUseCase = sendPendingConvocationsUseCase;
        this.remindPendingConvocationsUseCase = remindPendingConvocationsUseCase;
        this.recordConvocationDeliveryUseCase = recordConvocationDeliveryUseCase;
        this.replyToConvocationUseCase = replyToConvocationUseCase;
        this.checkInConvocationUseCase = checkInConvocationUseCase;
        this.getConvocationUseCase = getConvocationUseCase;
        this.getConvocationDocumentUseCase = getConvocationDocumentUseCase;
        this.listConvocationsByMeetingUseCase = listConvocationsByMeetingUseCase;
        this.getAttendanceSummaryUseCase = getAttendanceSummaryUseCase;
        this.listMyConvocationsUseCase = listMyConvocationsUseCase;
    }

    /**
     * Creates the convocations and convokes the meeting. Sends nothing.
     * Idempotent: find-or-create per lot, so a double click or a retry adds
     * nothing.
     */
    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/convocations")
    public ResponseEntity<List<ConvocationResponse>> generate(@PathVariable String meetingId) {
        List<ConvocationResponse> convocations = generateConvocationsUseCase
                .generate(new GenerateConvocationsCommand(GeneralMeetingId.of(meetingId)))
                .stream().map(ConvocationResponse::from).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(convocations);
    }

    /**
     * Sends every convocation still waiting to go out. Re-running it retries
     * exactly those, and never re-sends one that already left.
     */
    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/convocations/send")
    public SendConvocationsResultResponse sendPending(@PathVariable String meetingId,
                                                        @RequestBody(required = false) SendConvocationsRequest request,
                                                        Authentication authentication) {
        SendConvocationsRequest body = request == null ? new SendConvocationsRequest(null) : request;
        return SendConvocationsResultResponse.from(sendPendingConvocationsUseCase.send(
                new SendPendingConvocationsCommand(GeneralMeetingId.of(meetingId), body.channelOrDefault(),
                        currentUserId(authentication))));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @GetMapping("/general-meetings/{meetingId}/convocations")
    public List<ConvocationResponse> list(@PathVariable String meetingId,
                                            @RequestParam(required = false) String status) {
        return listConvocationsByMeetingUseCase.listConvocations(
                        new ListConvocationsByMeetingQuery(GeneralMeetingId.of(meetingId), parseStatus(status)))
                .stream().map(ConvocationResponse::from).toList();
    }

    @PreAuthorize("@propertyAccess.canReadMeeting(authentication, #meetingId)")
    @GetMapping("/general-meetings/{meetingId}/attendance-summary")
    public AttendanceSummaryResponse attendanceSummary(@PathVariable String meetingId) {
        return AttendanceSummaryResponse.from(getAttendanceSummaryUseCase.getSummary(
                new GetAttendanceSummaryQuery(GeneralMeetingId.of(meetingId))));
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/convocations/reminders")
    public ReminderResultResponse remind(@PathVariable String meetingId, Authentication authentication) {
        return new ReminderResultResponse(remindPendingConvocationsUseCase.remind(
                new RemindPendingConvocationsCommand(GeneralMeetingId.of(meetingId), currentUserId(authentication))));
    }

    @PreAuthorize("@propertyAccess.canManageConvocation(authentication, #id)")
    @GetMapping("/convocations/{id}")
    public ConvocationResponse get(@PathVariable String id) {
        return ConvocationResponse.from(
                getConvocationUseCase.getConvocation(new GetConvocationQuery(ConvocationId.of(id))));
    }

    /**
     * The convocation letter as a PDF - the assembly and its agenda, the lot,
     * and the owners it is addressed to.
     *
     * <p>Once sent, this is the very file that was filed at that moment, not a
     * re-render: it is the record of what the copropriétaire received. Before
     * the first send it is rendered on the fly - the preview, and the file a
     * syndic prints to convoke by post.
     *
     * <p>Readable by the lot's owner too (canReplyToConvocation is the same
     * "staff, or this lot's owner" rule): a copropriétaire is entitled to their
     * own convocation.
     */
    @PreAuthorize("@propertyAccess.canReplyToConvocation(authentication, #id)")
    @GetMapping("/convocations/{id}/document")
    public ResponseEntity<byte[]> document(@PathVariable String id) {
        GetConvocationDocumentUseCase.ConvocationDocument document =
                getConvocationDocumentUseCase.getDocument(new GetConvocationQuery(ConvocationId.of(id)));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + document.fileName() + "\"")
                .body(document.content());
    }

    /** Performs the send. Refuses the postal channels - those are recorded, not performed. */
    @PreAuthorize("@propertyAccess.canManageConvocation(authentication, #id)")
    @PostMapping("/convocations/{id}/send")
    public ConvocationResponse send(@PathVariable String id,
                                      @RequestBody(required = false) SendConvocationRequest request,
                                      Authentication authentication) {
        SendConvocationRequest body = request == null ? new SendConvocationRequest(null) : request;
        return ConvocationResponse.from(sendConvocationUseCase.send(new SendConvocationCommand(
                ConvocationId.of(id), body.channelOrDefault(), currentUserId(authentication))));
    }

    /** Records that a person delivered it - by post, by registered mail or by hand. */
    @PreAuthorize("@propertyAccess.canManageConvocation(authentication, #id)")
    @PutMapping("/convocations/{id}/delivery-status")
    public ConvocationResponse recordDelivery(@PathVariable String id,
                                                @Valid @RequestBody RecordConvocationDeliveryRequest request,
                                                Authentication authentication) {
        return ConvocationResponse.from(recordConvocationDeliveryUseCase.record(new RecordConvocationDeliveryCommand(
                ConvocationId.of(id), request.channelCode(), request.deliveryStatus(), request.reference(),
                currentUserId(authentication))));
    }

    /**
     * The owner answering for their own lot, or the syndic entering it on their
     * behalf. Which of the two it is - the reply's source - is settled from the
     * authenticated caller, never from the body: a request able to state its own
     * provenance could claim an answer the copropriétaire never gave.
     */
    @PreAuthorize("@propertyAccess.canReplyToConvocation(authentication, #id)")
    @PutMapping("/convocations/{id}/reply")
    public ConvocationResponse reply(@PathVariable String id, @Valid @RequestBody ReplyToConvocationRequest request,
                                      Authentication authentication) {
        ReplyMediumCode medium = request.medium() == null || request.medium().isBlank() ? null
                : ReplyMediumCode.of(request.medium());
        return ConvocationResponse.from(replyToConvocationUseCase.reply(new ReplyToConvocationCommand(
                ConvocationId.of(id), request.attendanceReply(), request.attendanceMode(), request.byProxy(), medium,
                request.receivedAt(), request.note(), currentUserId(authentication))));
    }

    @PreAuthorize("@propertyAccess.canManageConvocation(authentication, #id)")
    @PostMapping("/convocations/{id}/check-in")
    public ConvocationResponse checkIn(@PathVariable String id, @Valid @RequestBody CheckInConvocationRequest request) {
        EntityId partyId = request.checkedInPartyId() == null || request.checkedInPartyId().isBlank() ? null
                : EntityId.of(request.checkedInPartyId());
        return ConvocationResponse.from(checkInConvocationUseCase.checkIn(
                new CheckInConvocationCommand(ConvocationId.of(id), request.attendanceMode(), partyId)));
    }

    /** Undoes a sign-in entered by mistake - a room is ticked off by hand, and hands slip. */
    @PreAuthorize("@propertyAccess.canManageConvocation(authentication, #id)")
    @DeleteMapping("/convocations/{id}/check-in")
    public ConvocationResponse undoCheckIn(@PathVariable String id) {
        return ConvocationResponse.from(checkInConvocationUseCase.undoCheckIn(
                new CheckInConvocationCommand(ConvocationId.of(id), null, null)));
    }

    @GetMapping("/users/me/convocations")
    public List<MyConvocationResponse> listMine(Authentication authentication) {
        return listMyConvocationsUseCase.listMyConvocations(new ListMyConvocationsQuery(currentUserId(authentication)))
                .stream().map(MyConvocationResponse::from).toList();
    }

    private static ConvocationStatus parseStatus(String status) {
        return status == null || status.isBlank() ? null : ConvocationStatus.valueOf(status.toUpperCase(Locale.ROOT));
    }

    private static EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
