package com.architek.oikos.messaging.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.architek.oikos.messaging.application.command.CreateMessageDraftCommand;
import com.architek.oikos.messaging.application.command.UpdateMessageDraftCommand;
import com.architek.oikos.messaging.application.dto.MessageDraftView;
import com.architek.oikos.messaging.application.port.in.CreateMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.DeleteMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.GetMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.ListMyMessageDraftsUseCase;
import com.architek.oikos.messaging.application.port.in.SendMessageDraftUseCase;
import com.architek.oikos.messaging.application.port.in.UpdateMessageDraftUseCase;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageDraftId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.domain.model.PropertyRole;

@WebMvcTest(controllers = MessageDraftController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class MessageDraftControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private CreateMessageDraftUseCase createMessageDraftUseCase;

    @MockitoBean
    private UpdateMessageDraftUseCase updateMessageDraftUseCase;

    @MockitoBean
    private GetMessageDraftUseCase getMessageDraftUseCase;

    @MockitoBean
    private ListMyMessageDraftsUseCase listMyMessageDraftsUseCase;

    @MockitoBean
    private DeleteMessageDraftUseCase deleteMessageDraftUseCase;

    @MockitoBean
    private SendMessageDraftUseCase sendMessageDraftUseCase;

    private UUID callerId;

    private String bearerToken(String... authorities) {
        callerId = UUID.randomUUID();
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(callerId), Set.of(authorities));
    }

    private void ownedBy(MessageDraftId draftId, EntityId owner) {
        when(getMessageDraftUseCase.getDraft(any()))
                .thenReturn(new MessageDraftView(draftId, EntityId.newId(), owner, List.of(), false, "Sujet", "Corps"));
    }

    @Test
    void a_property_member_can_create_a_draft() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("PROPERTY_OWNER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(
                Set.of(), Map.of(propertyId, Set.of(PropertyRole.PROPERTY_OWNER)), Map.of(), Set.of(), Set.of()));
        when(createMessageDraftUseCase.create(any())).thenReturn(MessageDraftId.newId());

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/message-drafts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":[],"broadcast":false,"subject":null,"body":null}
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<CreateMessageDraftCommand> captor = ArgumentCaptor.forClass(CreateMessageDraftCommand.class);
        verify(createMessageDraftUseCase).create(captor.capture());
        assertThat(captor.getValue().propertyId()).isEqualTo(EntityId.of(propertyId));
        assertThat(captor.getValue().ownerId()).isEqualTo(EntityId.of(callerId));
    }

    @Test
    void a_non_member_cannot_create_a_draft_on_a_property_they_do_not_belong_to() throws Exception {
        String propertyId = UUID.randomUUID().toString();
        String token = bearerToken("ROLE_USER");
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(Set.of(), Map.of(), Map.of(), Set.of(), Set.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/message-drafts")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":[],"broadcast":false,"subject":null,"body":null}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void the_owner_can_update_their_own_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.of(callerId));

        mockMvc.perform(put("/api/v1/message-drafts/" + draftId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":[],"broadcast":false,"subject":"New","body":"New body"}
                                """))
                .andExpect(status().isNoContent());

        ArgumentCaptor<UpdateMessageDraftCommand> captor = ArgumentCaptor.forClass(UpdateMessageDraftCommand.class);
        verify(updateMessageDraftUseCase).update(captor.capture());
        assertThat(captor.getValue().payload().subject()).isEqualTo("New");
    }

    @Test
    void someone_else_cannot_update_another_user_s_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.newId());

        mockMvc.perform(put("/api/v1/message-drafts/" + draftId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"recipientUserIds":[],"broadcast":false,"subject":"New","body":"New body"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void the_owner_can_fetch_their_own_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.of(callerId));

        mockMvc.perform(get("/api/v1/message-drafts/" + draftId).header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void lists_the_caller_s_drafts() throws Exception {
        when(listMyMessageDraftsUseCase.listDrafts(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/users/me/message-drafts").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void the_owner_can_delete_their_own_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.of(callerId));

        mockMvc.perform(delete("/api/v1/message-drafts/" + draftId).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void the_owner_can_send_their_own_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.of(callerId));
        when(sendMessageDraftUseCase.send(any())).thenReturn(ConversationId.newId());

        mockMvc.perform(post("/api/v1/message-drafts/" + draftId + "/send").header("Authorization", token))
                .andExpect(status().isCreated());
    }

    @Test
    void someone_else_cannot_send_another_user_s_draft() throws Exception {
        MessageDraftId draftId = MessageDraftId.newId();
        String token = bearerToken("ROLE_USER");
        ownedBy(draftId, EntityId.newId());

        mockMvc.perform(post("/api/v1/message-drafts/" + draftId + "/send").header("Authorization", token))
                .andExpect(status().isForbidden());
    }
}
