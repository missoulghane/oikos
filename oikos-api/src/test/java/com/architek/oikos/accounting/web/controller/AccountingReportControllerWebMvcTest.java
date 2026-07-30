package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.accounting.application.dto.TreasurySummaryView;
import com.architek.oikos.accounting.application.port.in.GetTreasurySummaryUseCase;
import com.architek.oikos.accounting.application.port.in.ListFinancialJournalEntriesUseCase;
import com.architek.oikos.accounting.application.port.in.ListUnitAccountSummariesByPropertyUseCase;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = AccountingReportController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class AccountingReportControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private GetTreasurySummaryUseCase getTreasurySummaryUseCase;

    @MockitoBean
    private ListUnitAccountSummariesByPropertyUseCase listUnitAccountSummariesByPropertyUseCase;

    @MockitoBean
    private ListFinancialJournalEntriesUseCase listFinancialJournalEntriesUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/treasury-summary"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_read_the_treasury_summary() throws Exception {
        when(getTreasurySummaryUseCase.get(any())).thenReturn(
                new TreasurySummaryView(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/treasury-summary")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_list_unit_account_summaries() throws Exception {
        when(listUnitAccountSummariesByPropertyUseCase.list(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/units-summary")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_list_the_journal() throws Exception {
        when(listFinancialJournalEntriesUseCase.list(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/journal")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_reading_the_treasury_summary() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/treasury-summary")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
