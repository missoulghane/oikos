package com.architek.oikos.meeting.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.architek.oikos.meeting.application.command.AddAgendaItemCommand;
import com.architek.oikos.meeting.application.command.DeleteAgendaItemCommand;
import com.architek.oikos.meeting.application.command.ReorderAgendaItemsCommand;
import com.architek.oikos.meeting.application.command.UpdateAgendaItemCommand;
import com.architek.oikos.meeting.application.port.in.AddAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.DeleteAgendaItemUseCase;
import com.architek.oikos.meeting.application.port.in.ListAgendaItemsUseCase;
import com.architek.oikos.meeting.application.port.in.ReorderAgendaItemsUseCase;
import com.architek.oikos.meeting.application.port.in.UpdateAgendaItemUseCase;
import com.architek.oikos.meeting.application.query.ListAgendaItemsQuery;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.web.request.AgendaItemRequest;
import com.architek.oikos.meeting.web.request.ReorderAgendaItemsRequest;
import com.architek.oikos.meeting.web.response.AgendaItemResponse;

/**
 * The agenda of one meeting, editable at every status.
 *
 * <p>The strict rule - an owner is convoked on the strength of a fixed list,
 * so nothing may be added afterwards - was implemented and then deliberately
 * lifted (ADR 0002 §8): no functional blocking rule for the time being. The
 * ADR records where to put it back if it becomes necessary.
 */
@RestController
public class AgendaItemController {

    private final AddAgendaItemUseCase addAgendaItemUseCase;
    private final UpdateAgendaItemUseCase updateAgendaItemUseCase;
    private final DeleteAgendaItemUseCase deleteAgendaItemUseCase;
    private final ReorderAgendaItemsUseCase reorderAgendaItemsUseCase;
    private final ListAgendaItemsUseCase listAgendaItemsUseCase;

    public AgendaItemController(AddAgendaItemUseCase addAgendaItemUseCase,
                                 UpdateAgendaItemUseCase updateAgendaItemUseCase,
                                 DeleteAgendaItemUseCase deleteAgendaItemUseCase,
                                 ReorderAgendaItemsUseCase reorderAgendaItemsUseCase,
                                 ListAgendaItemsUseCase listAgendaItemsUseCase) {
        this.addAgendaItemUseCase = addAgendaItemUseCase;
        this.updateAgendaItemUseCase = updateAgendaItemUseCase;
        this.deleteAgendaItemUseCase = deleteAgendaItemUseCase;
        this.reorderAgendaItemsUseCase = reorderAgendaItemsUseCase;
        this.listAgendaItemsUseCase = listAgendaItemsUseCase;
    }

    @PreAuthorize("@propertyAccess.canReadMeeting(authentication, #meetingId)")
    @GetMapping("/general-meetings/{meetingId}/agenda-items")
    public List<AgendaItemResponse> list(@PathVariable String meetingId) {
        return listAgendaItemsUseCase.listAgendaItems(new ListAgendaItemsQuery(GeneralMeetingId.of(meetingId)))
                .stream().map(AgendaItemResponse::from).toList();
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PostMapping("/general-meetings/{meetingId}/agenda-items")
    public ResponseEntity<AgendaItemResponse> add(@PathVariable String meetingId,
                                                    @Valid @RequestBody AgendaItemRequest request) {
        AgendaItemResponse response = AgendaItemResponse.from(addAgendaItemUseCase.add(new AddAgendaItemCommand(
                GeneralMeetingId.of(meetingId), request.label(), request.description(), request.majorityRule())));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.canManageMeeting(authentication, #meetingId)")
    @PutMapping("/general-meetings/{meetingId}/agenda-items/order")
    public List<AgendaItemResponse> reorder(@PathVariable String meetingId,
                                              @Valid @RequestBody ReorderAgendaItemsRequest request) {
        List<AgendaItemId> orderedIds = request.orderedItemIds().stream().map(AgendaItemId::of).toList();
        return reorderAgendaItemsUseCase.reorder(new ReorderAgendaItemsCommand(GeneralMeetingId.of(meetingId), orderedIds))
                .stream().map(AgendaItemResponse::from).toList();
    }

    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @PutMapping("/agenda-items/{id}")
    public AgendaItemResponse update(@PathVariable String id, @Valid @RequestBody AgendaItemRequest request) {
        return AgendaItemResponse.from(updateAgendaItemUseCase.update(new UpdateAgendaItemCommand(
                AgendaItemId.of(id), request.label(), request.description(), request.majorityRule())));
    }

    @PreAuthorize("@propertyAccess.canManageAgendaItem(authentication, #id)")
    @DeleteMapping("/agenda-items/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteAgendaItemUseCase.delete(new DeleteAgendaItemCommand(AgendaItemId.of(id)));
        return ResponseEntity.noContent().build();
    }
}
