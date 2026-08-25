package com.architek.oikos.messaging.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.messaging.application.dto.ConversationView;
import com.architek.oikos.messaging.application.query.ConversationBox;
import com.architek.oikos.messaging.application.query.ListMyConversationsQuery;
import com.architek.oikos.messaging.application.dto.MessageView;
import com.architek.oikos.messaging.application.dto.RecipientCandidateView;
import com.architek.oikos.messaging.application.dto.UnreadSummaryView;
import com.architek.oikos.messaging.application.port.in.GetConversationUseCase;
import com.architek.oikos.messaging.application.port.in.GetUnreadSummaryUseCase;
import com.architek.oikos.messaging.application.port.in.ListConversationMessagesUseCase;
import com.architek.oikos.messaging.application.port.in.ListMyConversationsUseCase;
import com.architek.oikos.messaging.application.port.in.ListRecipientCandidatesUseCase;
import com.architek.oikos.messaging.application.port.in.MarkConversationReadUseCase;
import com.architek.oikos.messaging.application.port.in.MarkConversationUnreadUseCase;
import com.architek.oikos.messaging.application.port.in.SendBroadcastMessageUseCase;
import com.architek.oikos.messaging.application.port.in.SendMessageUseCase;
import com.architek.oikos.messaging.application.port.in.StartBoardConversationUseCase;
import com.architek.oikos.messaging.application.port.in.StartGroupConversationUseCase;
import com.architek.oikos.messaging.domain.model.ConversationType;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.model.PropertyRole;

@WebMvcTest(controllers = ConversationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class ConversationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private StartGroupConversationUseCase startGroupConversationUseCase;

    @MockitoBean
    private StartBoardConversationUseCase startBoardConversationUseCase;

    @MockitoBean
    private SendBroadcastMessageUseCase sendBroadcastMessageUseCase;

    @MockitoBean
    private ListMyConversationsUseCase listMyConversationsUseCase;

    @MockitoBean
    private GetUnreadSummaryUseCase getUnreadSummaryUseCase;

    @MockitoBean
    private ListConversationMessagesUseCase listConversationMessagesUseCase;

    @MockitoBean
    private SendMessageUseCase sendMessageUseCase;

    @MockitoBean
    private MarkConversationReadUseCase markConversationReadUseCase;

    @MockitoBean
    private MarkConversationUnreadUseCase markConversationUnreadUseCase;

    @MockitoBean
    private ListRecipientCandidatesUseCase listRecipientCandidatesUseCase;

    @MockitoBean
    private GetConversationUseCase getConversationUseCase;

    private UUID callerId;

    private String bearerToken(String... authorities) {
        callerId = UUID.randomUUID();
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(callerId), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me/conversations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void a_non_member_cannot_start_a_conversation_on_a_property() throws Exception {
        String propertyId = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/conversations")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":["%s"],"subject":"Sujet","body":"Bonjour"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_property_member_can_start_a_conversation() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));
        when(startGroupConversationUseCase.start(any())).thenReturn(ConversationId.newId());

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":["%s","%s"],"subject":"Sujet","body":"Bonjour à vous deux"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void starting_a_conversation_with_an_empty_recipient_list_is_rejected() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":[],"subject":"Sujet","body":"Bonjour"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void starting_a_conversation_with_a_blank_body_is_rejected() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":["%s"],"subject":"Sujet","body":"   "}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void starting_a_conversation_with_a_blank_subject_is_rejected() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/conversations")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":["%s"],"subject":"   ","body":"Bonjour"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void a_plain_owner_cannot_broadcast() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/broadcast-messages")
                        .header("Authorization", bearerToken("PROPERTY_OWNER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject":"Coupure d'eau","body":"Annonce"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_board_admin_can_broadcast() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.MESSAGING_BROADCAST)), Set.of(), Set.of()));
        when(sendBroadcastMessageUseCase.send(any())).thenReturn(ConversationId.newId());

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/broadcast-messages")
                        .header("Authorization", bearerToken("PROPERTY_BOARD_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject":"Coupure d'eau","body":"Annonce"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void broadcasting_with_a_blank_body_is_rejected() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.MESSAGING_BROADCAST)), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/broadcast-messages")
                        .header("Authorization", bearerToken("PROPERTY_BOARD_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subject":"Coupure d'eau","body":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lists_the_caller_s_conversations() throws Exception {
        when(listMyConversationsUseCase.listConversations(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users/me/conversations")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void the_box_query_param_is_parsed_and_forwarded_case_insensitively() throws Exception {
        when(listMyConversationsUseCase.listConversations(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users/me/conversations?box=sent")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());

        ArgumentCaptor<ListMyConversationsQuery> captor = ArgumentCaptor.forClass(ListMyConversationsQuery.class);
        verify(listMyConversationsUseCase).listConversations(captor.capture());
        assertThat(captor.getValue().box()).isEqualTo(ConversationBox.SENT);
    }

    @Test
    void the_box_query_param_defaults_to_unfiltered_when_absent() throws Exception {
        when(listMyConversationsUseCase.listConversations(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users/me/conversations")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());

        ArgumentCaptor<ListMyConversationsQuery> captor = ArgumentCaptor.forClass(ListMyConversationsQuery.class);
        verify(listMyConversationsUseCase).listConversations(captor.capture());
        assertThat(captor.getValue().box()).isNull();
    }

    @Test
    void gets_the_unread_summary() throws Exception {
        when(getUnreadSummaryUseCase.getSummary(any())).thenReturn(new UnreadSummaryView(0, 0, List.of()));

        mockMvc.perform(get("/api/v1/users/me/conversations/unread-summary")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void a_group_participant_can_list_messages_of_their_conversation() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        String token = bearerToken("ROLE_USER");
        EntityId caller = EntityId.of(callerId);
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.newId(), ConversationType.GROUP, caller, Set.of(caller, EntityId.newId())));
        when(listConversationMessagesUseCase.listMessages(any())).thenReturn(Page.of(List.of(), 0, 50, 0));

        mockMvc.perform(get("/api/v1/conversations/" + conversationId + "/messages")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void a_stranger_cannot_list_messages_of_a_group_conversation_they_are_not_part_of() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.newId(), ConversationType.GROUP, EntityId.newId(), Set.of(EntityId.newId(), EntityId.newId())));

        mockMvc.perform(get("/api/v1/conversations/" + conversationId + "/messages")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_plain_property_member_can_read_the_broadcast_conversation() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.of(propertyId), ConversationType.BROADCAST, EntityId.newId(), Set.of()));
        when(listConversationMessagesUseCase.listMessages(any())).thenReturn(Page.of(List.of(), 0, 50, 0));

        mockMvc.perform(get("/api/v1/conversations/" + conversationId + "/messages")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void a_plain_property_member_cannot_reply_in_the_broadcast_conversation() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.of(propertyId), ConversationType.BROADCAST, EntityId.newId(), Set.of()));

        mockMvc.perform(post("/api/v1/conversations/" + conversationId + "/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"body":"Merci pour l'info"}
                                """))
                .andExpect(status().isForbidden());
    }

    // Un envoi groupé n'est pas un fil : même le bureau n'y répond pas, il en
    // émet un autre (voir SendBroadcastMessageService).
    @Test
    void not_even_a_board_member_can_reply_in_a_broadcast() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_BOARD_ADMIN");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(), Map.of(propertyId, Set.of(Permission.MESSAGING_BROADCAST)), Set.of(), Set.of()));
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.of(propertyId), ConversationType.BROADCAST, EntityId.newId(), Set.of()));

        mockMvc.perform(post("/api/v1/conversations/" + conversationId + "/messages")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"body":"Merci pour l'info"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void marking_a_conversation_read_returns_204() throws Exception {
        ConversationId conversationId = ConversationId.newId();
        String token = bearerToken("ROLE_USER");
        EntityId caller = EntityId.of(callerId);
        when(getConversationUseCase.getConversation(any())).thenReturn(new ConversationView(
                conversationId, EntityId.newId(), ConversationType.GROUP, caller, Set.of(caller, EntityId.newId())));

        mockMvc.perform(post("/api/v1/conversations/" + conversationId + "/read")
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void a_non_member_cannot_list_recipient_candidates() throws Exception {
        String propertyId = UUID.randomUUID().toString();

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/messaging/recipients")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void a_property_member_can_list_recipient_candidates() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));
        when(listRecipientCandidatesUseCase.listCandidates(any())).thenReturn(List.of(
                new RecipientCandidateView(EntityId.newId(), "Jean Dupont", "Bureau de syndic", List.of(), true)));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/messaging/recipients")
                        .header("Authorization", bearerToken("PROPERTY_OWNER")))
                .andExpect(status().isOk());
    }
}
