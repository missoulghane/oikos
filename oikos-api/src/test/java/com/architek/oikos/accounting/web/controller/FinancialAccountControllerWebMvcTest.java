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

import com.architek.oikos.accounting.application.port.in.CreateFinancialAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ListFinancialAccountsByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.TransferBetweenFinancialAccountsUseCase;
import com.architek.oikos.accounting.domain.valueobject.FinancialAccountId;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = FinancialAccountController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class FinancialAccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreateFinancialAccountUseCase createFinancialAccountUseCase;

    @MockitoBean
    private ListFinancialAccountsByPropertyUseCase listFinancialAccountsByPropertyUseCase;

    @MockitoBean
    private TransferBetweenFinancialAccountsUseCase transferBetweenFinancialAccountsUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/financial-accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_create_a_financial_account() throws Exception {
        when(createFinancialAccountUseCase.create(any())).thenReturn(FinancialAccountId.newId());

        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/financial-accounts")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Caisse","type":"CASH","currency":"MAD"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_list_financial_accounts() throws Exception {
        when(listFinancialAccountsByPropertyUseCase.list(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/financial-accounts")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_transfer_between_two_financial_accounts() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/financial-accounts/transfers")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fromAccountId":"%s","toAccountId":"%s","amount":100,"date":"2026-03-01","label":"Depot"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isNoContent());
    }

    @Test
    void regular_user_is_forbidden_from_creating_a_financial_account() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/financial-accounts")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Caisse","type":"CASH","currency":"MAD"}
                                """))
                .andExpect(status().isForbidden());
    }
}
