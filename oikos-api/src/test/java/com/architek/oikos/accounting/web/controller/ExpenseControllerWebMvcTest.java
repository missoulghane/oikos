package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.accounting.application.port.in.ListExpensesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.RecordExpenseUseCase;
import com.architek.oikos.accounting.domain.valueobject.ExpenseId;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = ExpenseController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class ExpenseControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RecordExpenseUseCase recordExpenseUseCase;

    @MockitoBean
    private ListExpensesByPropertyUseCase listExpensesByPropertyUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/expenses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_record_an_expense() throws Exception {
        when(recordExpenseUseCase.record(any())).thenReturn(ExpenseId.newId());

        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/expenses")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"financialAccountId":"%s","date":"2026-03-01","category":"Gardiennage","provider":"Securitas","amount":300}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_list_expenses() throws Exception {
        when(listExpensesByPropertyUseCase.list(any())).thenReturn(Page.of(java.util.List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/expenses")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_recording_an_expense() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/expenses")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"financialAccountId":"%s","date":"2026-03-01","category":"Gardiennage","provider":"Securitas","amount":300}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
