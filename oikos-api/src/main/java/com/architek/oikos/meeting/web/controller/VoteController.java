package com.architek.oikos.meeting.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.meeting.application.command.CastVoteCommand;
import com.architek.oikos.meeting.application.command.CloseVoteSessionCommand;
import com.architek.oikos.meeting.application.command.OpenVoteSessionCommand;
import com.architek.oikos.meeting.application.command.RecordShowOfHandsCommand;
import com.architek.oikos.meeting.application.port.in.CastVoteUseCase;
import com.architek.oikos.meeting.application.port.in.CloseVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.GetAgendaItemResultUseCase;
import com.architek.oikos.meeting.application.port.in.ListVotesByAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.OpenVoteSessionUseCase;
import com.architek.oikos.meeting.application.port.in.RecordShowOfHandsUseCase;
import com.architek.oikos.meeting.application.query.GetAgendaItemResultQuery;
import com.architek.oikos.meeting.application.query.ListVotesByAgendaItemQuery;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.web.request.CastVoteRequest;
import com.architek.oikos.meeting.web.request.RecordShowOfHandsRequest;
import com.architek.oikos.meeting.web.response.AgendaItemResponse;
import com.architek.oikos.meeting.web.response.AgendaItemResultResponse;
import com.architek.oikos.meeting.web.response.VoteResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The ballot on one agenda item: opening, entering the votes, closing,
 * reading the result.
 *
 * <p>Every endpoint here is gated by meeting:manage, including the casting
 * ones. Votes are entered from the chair - by hand or as a show of hands -
 * because voting requires being present at the session (ADR 0002 §7: no
 * remote or out-of-session voting in v1). A copropriétaire's self-service
 * stops at answering their convocation.
 *
 * <p>The result is readable at any time, and is recomputed on each read. Mid
 * ballot it shows where the vote stands; it becomes final only when the
 * minutes freeze it.
 */
@RestController
public class VoteController {

    private final OpenVoteSessionUseCase openVoteSessionUseCase;
    private final CloseVoteSessionUseCase closeVoteSessionUseCase;
    private final CastVoteUseCase castVoteUseCase;
    private final RecordShowOfHandsUseCase recordShowOfHandsUseCase;
    private final ListVotesByAgendaItemUseCase listVotesByAgendaItemUseCase;
    private final GetAgendaItemResultUseCase getAgendaItemResultUseCase;

    public VoteController(OpenVoteSessionUseCase openVoteSessionUseCase,
                           CloseVoteSessionUseCase closeVoteSessionUseCase, CastVoteUseCase castVoteUseCase,
                           RecordShowOfHandsUseCase recordShowOfHandsUseCase,
                           ListVotesByAgendaItemUseCase listVotesByAgendaItemUseCase,
                           GetAgendaItemResultUseCase getAgendaItemResultUseCase) {
        this.openVoteSessionUseCase = openVoteSessionUseCase;
        this.closeVoteSessionUseCase = closeVoteSessionUseCase;
        this.castVoteUseCase = castVoteUseCase;
        this.recordShowOfHandsUseCase = recordShowOfHandsUseCase;
        this.listVotesByAgendaItemUseCase = listVotesByAgendaItemUseCase;
        this.getAgendaItemResultUseCase = getAgendaItemResultUseCase;
    }

    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @PostMapping("/agenda-items/{id}/vote-session/open")
    public AgendaItemResponse openBallot(@PathVariable String id) {
        return AgendaItemResponse.from(openVoteSessionUseCase.open(
                new OpenVoteSessionCommand(AgendaItemId.of(id))));
    }

    /** Returns the result as of closing - the figures the chair announces to the room. */
    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @PostMapping("/agenda-items/{id}/vote-session/close")
    public AgendaItemResultResponse closeBallot(@PathVariable String id) {
        return AgendaItemResultResponse.from(closeVoteSessionUseCase.close(
                new CloseVoteSessionCommand(AgendaItemId.of(id))));
    }

    /** Nominal entry, one lot at a time. Casting again while the ballot is open replaces the choice. */
    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @PostMapping("/agenda-items/{id}/votes")
    public ResponseEntity<VoteResponse> cast(@PathVariable String id, @Valid @RequestBody CastVoteRequest request,
                                               Authentication authentication) {
        VoteResponse response = VoteResponse.from(castVoteUseCase.cast(new CastVoteCommand(AgendaItemId.of(id),
                EntityId.of(request.unitId()), request.choice(), currentUserId(authentication))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Show of hands: one choice for the lots present, with named exceptions. */
    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @PostMapping("/agenda-items/{id}/votes/show-of-hands")
    public AgendaItemResultResponse showOfHands(@PathVariable String id,
                                                  @Valid @RequestBody RecordShowOfHandsRequest request,
                                                  Authentication authentication) {
        Map<EntityId, VoteChoice> exceptions = request.exceptionsOrEmpty().entrySet().stream()
                .collect(Collectors.toMap(entry -> EntityId.of(entry.getKey()), Map.Entry::getValue));
        return AgendaItemResultResponse.from(recordShowOfHandsUseCase.record(new RecordShowOfHandsCommand(
                AgendaItemId.of(id), request.defaultChoice(), exceptions, currentUserId(authentication))));
    }

    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @GetMapping("/agenda-items/{id}/votes")
    public List<VoteResponse> listVotes(@PathVariable String id) {
        return listVotesByAgendaItemUseCase.listVotes(new ListVotesByAgendaItemQuery(AgendaItemId.of(id)))
                .stream().map(VoteResponse::from).toList();
    }

    /** Readable by every member of the copropriété: a result is not a secret. */
    @PreAuthorize("@propertyAccess.canReadAgendaItem(authentication, #id)")
    @GetMapping("/agenda-items/{id}/result")
    public AgendaItemResultResponse result(@PathVariable String id) {
        return AgendaItemResultResponse.from(getAgendaItemResultUseCase.getResult(
                new GetAgendaItemResultQuery(AgendaItemId.of(id))));
    }

    private static EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
