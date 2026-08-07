package com.architek.oikos.property.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.command.RemoveBoardMemberCommand;
import com.architek.oikos.property.application.command.ValidateBoardMemberCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveBoardMemberUseCase;
import com.architek.oikos.property.application.port.in.ValidateBoardMemberUseCase;
import com.architek.oikos.property.application.query.ListBoardMembersByPropertyQuery;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.property.web.request.AddBoardMemberRequest;
import com.architek.oikos.property.web.response.BoardMemberResponse;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Rattachement des membres du syndic (via Party) a une copropriété.
 */
@RestController
public class BoardMemberController {

    private final AddBoardMemberUseCase addBoardMemberUseCase;
    private final ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase;
    private final RemoveBoardMemberUseCase removeBoardMemberUseCase;
    private final ValidateBoardMemberUseCase validateBoardMemberUseCase;

    public BoardMemberController(AddBoardMemberUseCase addBoardMemberUseCase,
                                   ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase,
                                   RemoveBoardMemberUseCase removeBoardMemberUseCase,
                                   ValidateBoardMemberUseCase validateBoardMemberUseCase) {
        this.addBoardMemberUseCase = addBoardMemberUseCase;
        this.listBoardMembersByPropertyUseCase = listBoardMembersByPropertyUseCase;
        this.removeBoardMemberUseCase = removeBoardMemberUseCase;
        this.validateBoardMemberUseCase = validateBoardMemberUseCase;
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/board-members")
    public List<BoardMemberResponse> list(@PathVariable String propertyId) {
        ListBoardMembersByPropertyQuery query = new ListBoardMembersByPropertyQuery(PropertyId.of(propertyId));
        return listBoardMembersByPropertyUseCase.listBoardMembers(query).stream()
                .map(BoardMemberResponse::from)
                .toList();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/board-members")
    public ResponseEntity<Void> add(@PathVariable String propertyId, @Valid @RequestBody AddBoardMemberRequest request) {
        BoardMemberId id = addBoardMemberUseCase.add(new AddBoardMemberCommand(
                PropertyId.of(propertyId), request.partyId() != null ? EntityId.of(request.partyId()) : null,
                request.fullName(), request.email(), request.phone(), request.boardRole()));
        return ResponseEntity.created(URI.create("/api/v1/properties/" + propertyId + "/board-members/" + id)).build();
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/properties/{propertyId}/board-members/{id}")
    public void remove(@PathVariable String propertyId, @PathVariable String id) {
        removeBoardMemberUseCase.remove(new RemoveBoardMemberCommand(BoardMemberId.of(id)));
    }

    @PreAuthorize("@propertyAccess.managesProperty(authentication, #propertyId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PatchMapping("/properties/{propertyId}/board-members/{id}/validate")
    public void validate(@PathVariable String propertyId, @PathVariable String id) {
        validateBoardMemberUseCase.validate(new ValidateBoardMemberCommand(BoardMemberId.of(id)));
    }
}
