package com.architek.oikos.messaging.web.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.auth.infrastructure.security.UserPrincipal;
import com.architek.oikos.messaging.application.command.MarkConversationReadCommand;
import com.architek.oikos.messaging.application.command.SendBroadcastMessageCommand;
import com.architek.oikos.messaging.application.command.SendMessageCommand;
import com.architek.oikos.messaging.application.command.StartGroupConversationCommand;
import com.architek.oikos.messaging.application.port.in.GetUnreadSummaryUseCase;
import com.architek.oikos.messaging.application.port.in.ListConversationMessagesUseCase;
import com.architek.oikos.messaging.application.port.in.ListMyConversationsUseCase;
import com.architek.oikos.messaging.application.port.in.ListRecipientCandidatesUseCase;
import com.architek.oikos.messaging.application.port.in.MarkConversationReadUseCase;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.application.port.in.SendMessageUseCase;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.application.query.GetUnreadSummaryQuery;
import com.architek.oikos.messaging.application.query.ListConversationMessagesQuery;
import com.architek.oikos.messaging.application.query.ListMyConversationsQuery;
import com.architek.oikos.messaging.application.query.ListRecipientCandidatesQuery;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.ConversationSubject;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.web.request.SendMessageRequest;
import com.architek.oikos.messaging.web.request.StartConversationRequest;
import com.architek.oikos.messaging.web.response.ConversationReferenceResponse;
import com.architek.oikos.messaging.web.response.MessageResponse;
import com.architek.oikos.messaging.web.response.PagedConversationSummaryResponse;
import com.architek.oikos.messaging.web.response.PagedMessageResponse;
import com.architek.oikos.messaging.web.response.RecipientCandidateResponse;
import com.architek.oikos.messaging.web.response.UnreadSummaryResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class ConversationController {

    private final StartGroupConversationUseCase startGroupConversationUseCase;
    private final SendBroadcastMessageUseCase sendBroadcastMessageUseCase;
    private final ListMyConversationsUseCase listMyConversationsUseCase;
    private final GetUnreadSummaryUseCase getUnreadSummaryUseCase;
    private final ListConversationMessagesUseCase listConversationMessagesUseCase;
    private final SendMessageUseCase sendMessageUseCase;
    private final MarkConversationReadUseCase markConversationReadUseCase;
    private final ListRecipientCandidatesUseCase listRecipientCandidatesUseCase;

    public ConversationController(StartGroupConversationUseCase startGroupConversationUseCase,
                                   SendBroadcastMessageUseCase sendBroadcastMessageUseCase,
                                   ListMyConversationsUseCase listMyConversationsUseCase,
                                   GetUnreadSummaryUseCase getUnreadSummaryUseCase,
                                   ListConversationMessagesUseCase listConversationMessagesUseCase,
                                   SendMessageUseCase sendMessageUseCase,
                                   MarkConversationReadUseCase markConversationReadUseCase,
                                   ListRecipientCandidatesUseCase listRecipientCandidatesUseCase) {
        this.startGroupConversationUseCase = startGroupConversationUseCase;
        this.sendBroadcastMessageUseCase = sendBroadcastMessageUseCase;
        this.listMyConversationsUseCase = listMyConversationsUseCase;
        this.getUnreadSummaryUseCase = getUnreadSummaryUseCase;
        this.listConversationMessagesUseCase = listConversationMessagesUseCase;
        this.sendMessageUseCase = sendMessageUseCase;
        this.markConversationReadUseCase = markConversationReadUseCase;
        this.listRecipientCandidatesUseCase = listRecipientCandidatesUseCase;
    }

    @PreAuthorize("@propertyAccess.isPropertyMember(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/conversations")
    public ResponseEntity<ConversationReferenceResponse> startConversation(@PathVariable String propertyId,
                                                                            @Valid @RequestBody StartConversationRequest request,
                                                                            Authentication authentication) {
        Set<EntityId> recipientUserIds = request.recipientUserIds().stream().map(EntityId::of).collect(Collectors.toSet());
        ConversationId id = startGroupConversationUseCase.start(new StartGroupConversationCommand(
                EntityId.of(propertyId), currentUserId(authentication), recipientUserIds,
                ConversationSubject.of(request.subject()), MessageBody.of(request.body())));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversationReferenceResponse.from(id));
    }

    @PreAuthorize("@propertyAccess.canBroadcastOnProperty(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/broadcast-messages")
    public ResponseEntity<ConversationReferenceResponse> broadcast(@PathVariable String propertyId,
                                                                     @Valid @RequestBody SendMessageRequest request,
                                                                     Authentication authentication) {
        ConversationId id = sendBroadcastMessageUseCase.send(new SendBroadcastMessageCommand(
                EntityId.of(propertyId), currentUserId(authentication), MessageBody.of(request.body())));
        return ResponseEntity.status(HttpStatus.CREATED).body(ConversationReferenceResponse.from(id));
    }

    @GetMapping("/users/me/conversations")
    public PagedConversationSummaryResponse listMyConversations(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "20") int size,
                                                                  @RequestParam(required = false) String search,
                                                                  Authentication authentication) {
        return PagedConversationSummaryResponse.from(listMyConversationsUseCase.listConversations(
                new ListMyConversationsQuery(currentUserId(authentication), PageRequest.of(page, size), search)));
    }

    @GetMapping("/users/me/conversations/unread-summary")
    public UnreadSummaryResponse unreadSummary(Authentication authentication) {
        return UnreadSummaryResponse.from(getUnreadSummaryUseCase.getSummary(new GetUnreadSummaryQuery(currentUserId(authentication))));
    }

    @PreAuthorize("@propertyAccess.isConversationParticipant(authentication, #id)")
    @GetMapping("/conversations/{id}/messages")
    public PagedMessageResponse listMessages(@PathVariable String id,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "50") int size,
                                              Authentication authentication) {
        return PagedMessageResponse.from(listConversationMessagesUseCase.listMessages(
                new ListConversationMessagesQuery(ConversationId.of(id), currentUserId(authentication), PageRequest.of(page, size))));
    }

    @PreAuthorize("@propertyAccess.canSendToConversation(authentication, #id)")
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable String id, @Valid @RequestBody SendMessageRequest request,
                                                         Authentication authentication) {
        MessageResponse response = MessageResponse.from(sendMessageUseCase.send(new SendMessageCommand(
                ConversationId.of(id), currentUserId(authentication), MessageBody.of(request.body()))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.isConversationParticipant(authentication, #id)")
    @PostMapping("/conversations/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable String id, Authentication authentication) {
        markConversationReadUseCase.markRead(new MarkConversationReadCommand(ConversationId.of(id), currentUserId(authentication)));
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@propertyAccess.isPropertyMember(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/messaging/recipients")
    public List<RecipientCandidateResponse> listRecipients(@PathVariable String propertyId,
                                                             @RequestParam(required = false) String search,
                                                             Authentication authentication) {
        return listRecipientCandidatesUseCase.listCandidates(
                        new ListRecipientCandidatesQuery(EntityId.of(propertyId), currentUserId(authentication), search))
                .stream().map(RecipientCandidateResponse::from).toList();
    }

    private EntityId currentUserId(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return EntityId.of(principal.getUserId());
    }
}
