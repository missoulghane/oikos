package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import com.architek.oikos.accounting.application.dto.AccountView;
import com.architek.oikos.accounting.application.dto.BalanceView;
import com.architek.oikos.accounting.application.port.in.CreateAccountUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountBalanceUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountByHolderUseCase;
import com.architek.oikos.accounting.application.port.in.GetAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ListMovementsUseCase;
import com.architek.oikos.accounting.application.port.in.RecordPaymentUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountId;
import com.architek.oikos.accounting.domain.valueobject.AccountType;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = AccountController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class AccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private GetAccountUseCase getAccountUseCase;

    @MockitoBean
    private GetAccountByHolderUseCase getAccountByHolderUseCase;

    @MockitoBean
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    @MockitoBean
    private ListMovementsUseCase listMovementsUseCase;

    @MockitoBean
    private RecordPaymentUseCase recordPaymentUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/" + AccountId.newId())).andExpect(status().isUnauthorized());
    }

    @Test
    void creates_an_account() throws Exception {
        when(createAccountUseCase.create(any())).thenReturn(AccountId.newId());

        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"holderId":"%s","accountType":"UNIT"}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void creating_an_account_without_a_holder_id_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void gets_an_account_by_holder_and_type() throws Exception {
        AccountId id = AccountId.newId();
        EntityId holderId = EntityId.newId();
        when(getAccountByHolderUseCase.getAccount(any()))
                .thenReturn(new AccountView(id, holderId, AccountType.PROPERTY, BigDecimal.ZERO));

        mockMvc.perform(get("/api/v1/accounts?holderId=" + holderId + "&accountType=PROPERTY")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void gets_an_account_by_id() throws Exception {
        AccountId id = AccountId.newId();
        when(getAccountUseCase.getAccount(any())).thenReturn(new AccountView(id, EntityId.newId(), AccountType.UNIT, BigDecimal.ZERO));

        mockMvc.perform(get("/api/v1/accounts/" + id).header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void gets_the_account_balance() throws Exception {
        AccountId id = AccountId.newId();
        when(getAccountBalanceUseCase.getBalance(any())).thenReturn(new BalanceView(id, new BigDecimal("750")));

        mockMvc.perform(get("/api/v1/accounts/" + id + "/balance").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void lists_movements() throws Exception {
        AccountId id = AccountId.newId();
        when(listMovementsUseCase.listMovements(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/accounts/" + id + "/movements").header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }

    @Test
    void records_a_payment() throws Exception {
        AccountId id = AccountId.newId();
        when(recordPaymentUseCase.record(any())).thenReturn(MovementId.newId());

        mockMvc.perform(post("/api/v1/accounts/" + id + "/payments")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":500,"label":"Paiement"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void recording_a_payment_with_a_zero_amount_returns_400() throws Exception {
        AccountId id = AccountId.newId();

        mockMvc.perform(post("/api/v1/accounts/" + id + "/payments")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":0,"label":"Paiement"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
