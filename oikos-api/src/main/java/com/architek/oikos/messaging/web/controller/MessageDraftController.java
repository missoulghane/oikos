package com.architek.oikos.messaging.web.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
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
import com.architek.oikos.messaging.application.command.CreateMessageDraftCommand;
import com.architek.oikos.messaging.application.command.DeleteMessageDraftCommand;
import com.architek.oikos.messaging.application.command.SaveMessageDraftCommand;
import com.architek.oikos.messaging.application.command.SendMessageDraftCommand;
import com.architek.oikos.messaging.application.command.UpdateMessageDraftCommand;
import com.architek.oikos.messaging.application.port.in.CreateMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.DeleteMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.GetMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.ListMyMessageDraftsUseCase;
import com.architek.oikos.messaging.application.port.in.SendMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.UpdateMessageDraftUseCase;
import com.architek.oikos.messaging.application.query.GetMessageDraftQuery;
import com.architek.oikos.messaging.application.query.ListMyMessageDraftsQuery;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.messaging.web.request.SaveMessageDraftRequest;
import com.architek.oikos.messaging.web.response.ConversationReferenceResponse;
import com.architek.oikos.messaging.web.response.MessageDraftReferenceResponse;
import com.architek.oikos.messaging.web.response.MessageDraftResponse;
import com.architek.oikos.messaging.web.response.PagedMessageDraftSummaryResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * No functional difference from the caller's point of view between saving a
 * draft that will end up GROUP vs BROADCAST (see SaveMessageDraftRequest) -
 * same rationale as the compose form itself never exposing two separate
 * flows (RecipientPicker's "Toute la copropriété" pseudo-recipient).
 */
@RestController
public class MessageDraftController {

    private final CreateMessageDraftUseCase createMessageDraftUseCase;
    private final UpdateMessageDraftUseCase updateMessageDraftUseCase;
    private final GetMessageDraftUseCase getMessageDraftUseCase;
    private final ListMyMessageDraftsUseCase listMyMessageDraftsUseCase;
    private final DeleteMessageDraftUseCase deleteMessageDraftUseCase;
    private final SendMessageDraftUseCase sendMessageDraftUseCase;

    public MessageDraftController(CreateMessageDraftUseCase createMessageDraftUseCase,
                                   UpdateMessageDraftUseCase updateMessageDraftUseCase,
                                   GetMessageDraftUseCase getMessageDraftUseCase,
                                   ListMyMessageDraftsUseCase listMyMessageDraftsUseCase,
                                   DeleteMessageDraftUseCase deleteMessageDraftUseCase,
                                   SendMessageDraftUseCase sendMessageDraftUseCase) {
        this.createMessageDraftUseCase = createMessageDraftUseCase;
        this.updateMessageDraftUseCase = updateMessageDraftUseCase;
        this.getMessageDraftUseCase = getMessageDraftUseCase;
        this.listMyMessageDraftsUseCase = listMyMessageDraftsUseCase;
        this.deleteMessageDraftUseCase = deleteMessageDraftUseCase;
        this.sendMessageDraftUseCase = sendMessageDraftUseCase;
    }

    @PreAuthorize("@propertyAccess.isPropertyMember(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/message-drafts")
    public ResponseEntity<MessageDraftReferenceResponse> create(@PathVariable String propertyId,
                                                                  @Valid @RequestBody SaveMessageDraftRequest request,
                                                                  Authentication authentication) {
        MessageDraftId id = createMessageDraftUseCase.create(new CreateMessageDraftCommand(EntityId.of(propertyId),
                currentUserId(authentication), toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(MessageDraftReferenceResponse.from(id));
    }

    @PreAuthorize("@propertyAccess.isDraftOwner(authentication, #id)")
    @PutMapping("/message-drafts/{id}")
    public ResponseEntity<Void> update(@PathVariable String id, @Valid @RequestBody SaveMessageDraftRequest request) {
        updateMessageDraftUseCase.update(new UpdateMessageDraftCommand(MessageDraftId.of(id), toCommand(request)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.isDraftOwner(authentication, #id)")
    @GetMapping("/message-drafts/{id}")
    public MessageDraftResponse get(@PathVariable String id) {
        return MessageDraftResponse.from(getMessageDraftUseCase.getDraft(new GetMessageDraftQuery(MessageDraftId.of(id))));
    }

    @GetMapping("/users/me/message-drafts")
    public PagedMessageDraftSummaryResponse listMyDrafts(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int size,
                                                           @RequestParam(required = false) String search,
                                                           Authentication authentication) {
        return PagedMessageDraftSummaryResponse.from(listMyMessageDraftsUseCase.listDrafts(
                new ListMyMessageDraftsQuery(currentUserId(authentication), PageRequest.of(page, size), search)));
    }

    @PreAuthorize("@propertyAccess.isDraftOwner(authentication, #id)")
    @DeleteMapping("/message-drafts/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteMessageDraftUseCase.delete(new DeleteMessageDraftCommand(MessageDraftId.of(id)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.isDraftOwner(authentication, #id)")
    @PostMapping("/message-drafts/{id}/send")
    public ResponseEntity<ConversationReferenceResponse> send(@PathVariable String id) {
        var conversationId = sendMessageDraftUseCase.send(new SendMessageDraftCommand(MessageDraftId.of(id)));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversationReferenceResponse.from(conversationId));
    }

    private static SaveMessageDraftCommand toCommand(SaveMessageDraftRequest request) {
        Set<EntityId> recipientUserIds = request.recipientUserIds() == null ? Set.of()
                : request.recipientUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
        return new SaveMessageDraftCommand(recipientUserIds, request.broadcast(), request.subject(), request.body());
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
