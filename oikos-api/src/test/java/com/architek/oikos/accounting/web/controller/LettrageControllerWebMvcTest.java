package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

import com.architek.oikos.accounting.application.port.in.ListPendingLettragesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateBulkLettrageUseCase;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = LettrageController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class LettrageControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ListPendingLettragesByPropertyUseCase listPendingLettragesByPropertyUseCase;

    @MockitoBean
    private ValidateBulkLettrageUseCase validateBulkLettrageUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/lettrage/pending"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_pending_lettrages() throws Exception {
        when(listPendingLettragesByPropertyUseCase.list(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/lettrage/pending")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_validate_all_pending_lettrages() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/lettrage/validate")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void admin_can_validate_a_chosen_subset_of_units() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/lettrage/validate")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"unitIds":["%s"]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNoContent());
    }

    @Test
    void regular_user_is_forbidden_from_validating() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/lettrage/validate")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
