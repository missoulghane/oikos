package com.architek.oikos.property.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.in.ListBoardMembersByPropertyUseCase;
import com.architek.oikos.property.application.port.in.RemoveBoardMemberUseCase;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardMemberId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = BoardMemberController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class BoardMemberControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AddBoardMemberUseCase addBoardMemberUseCase;

    @MockitoBean
    private ListBoardMembersByPropertyUseCase listBoardMembersByPropertyUseCase;

    @MockitoBean
    private RemoveBoardMemberUseCase removeBoardMemberUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/board-members"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_membres_syndic_of_a_property() throws Exception {
        when(listBoardMembersByPropertyUseCase.listBoardMembers(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + PropertyId.newId() + "/board-members")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_add_a_board_member() throws Exception {
        when(addBoardMemberUseCase.add(any())).thenReturn(BoardMemberId.newId());

        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/board-members")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contactId":"%s","boardRole":"PRESIDENT"}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void regular_user_is_forbidden_from_adding_a_board_member() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + PropertyId.newId() + "/board-members")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contactId":"%s","boardRole":"PRESIDENT"}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_remove_a_board_member() throws Exception {
        mockMvc.perform(delete("/api/v1/properties/" + PropertyId.newId() + "/board-members/" + BoardMemberId.newId())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }
}
