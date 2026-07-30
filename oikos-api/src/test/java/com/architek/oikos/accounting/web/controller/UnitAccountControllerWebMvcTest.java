package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.accounting.application.dto.LettrageProposalView;
import com.architek.oikos.accounting.application.dto.UnitAccountView;
import com.architek.oikos.accounting.application.port.in.GetUnitAccountUseCase;
import com.architek.oikos.accounting.application.port.in.GetUnitLettrageProposalUseCase;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountMovementsUseCase;
import com.architek.oikos.accounting.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.accounting.application.port.in.RecordUnitAccountRegularizationUseCase;
import com.architek.oikos.accounting.application.port.in.ValidateUnitLettrageUseCase;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountId;
import com.architek.oikos.accounting.domain.valueobject.UnitAccountMovementId;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = UnitAccountController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class UnitAccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GetUnitAccountUseCase getUnitAccountUseCase;

    @MockitoBean
    private ListUnitAccountMovementsUseCase listUnitAccountMovementsUseCase;

    @MockitoBean
    private RecordOwnerPaymentUseCase recordOwnerPaymentUseCase;

    @MockitoBean
    private RecordUnitAccountRegularizationUseCase recordUnitAccountRegularizationUseCase;

    @MockitoBean
    private GetUnitLettrageProposalUseCase getUnitLettrageProposalUseCase;

    @MockitoBean
    private ValidateUnitLettrageUseCase validateUnitLettrageUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/units/" + UUID.randomUUID() + "/account")).andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_get_a_unit_account() throws Exception {
        EntityId unitId = EntityId.newId();
        when(getUnitAccountUseCase.get(any())).thenReturn(new UnitAccountView(UnitAccountId.newId(), unitId,
                java.math.BigDecimal.ZERO, Instant.now()));

        mockMvc.perform(get("/api/v1/units/" + unitId + "/account").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_list_unit_account_movements() throws Exception {
        when(listUnitAccountMovementsUseCase.list(any())).thenReturn(Page.of(java.util.List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/units/" + UUID.randomUUID() + "/account/movements")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_record_a_payment() throws Exception {
        when(recordOwnerPaymentUseCase.record(any())).thenReturn(UnitAccountMovementId.newId());

        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units/" + UUID.randomUUID() + "/payments")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"financialAccountId":"%s","amount":150,"date":"2026-03-01","label":"Paiement"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_record_a_regularization() throws Exception {
        when(recordUnitAccountRegularizationUseCase.record(any())).thenReturn(UnitAccountMovementId.newId());

        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units/" + UUID.randomUUID() + "/regularizations")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":50,"direction":"CREDIT","label":"Remise","reason":"Geste commercial"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_get_the_lettrage_proposal() throws Exception {
        when(getUnitLettrageProposalUseCase.get(any())).thenReturn(new LettrageProposalView(EntityId.newId(),
                java.util.List.of(), java.util.List.of(), java.util.List.of(), java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO));

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units/" + UUID.randomUUID()
                        + "/lettrage-proposal").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_validate_the_lettrage() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units/" + UUID.randomUUID()
                        + "/lettrage/validate").header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isNoContent());
    }

    @Test
    void regular_user_with_no_access_is_forbidden_from_recording_a_payment() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units/" + UUID.randomUUID() + "/payments")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"financialAccountId":"%s","amount":150,"date":"2026-03-01","label":"Paiement"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
