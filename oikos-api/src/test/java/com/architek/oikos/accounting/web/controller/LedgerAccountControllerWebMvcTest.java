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

import com.architek.oikos.accounting.application.dto.LedgerAccountView;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByTreasuryAccountUseCase;
import com.architek.oikos.accounting.application.port.in.ListLedgerAccountsByPropertyUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountNature;
import com.architek.oikos.accounting.domain.valueobject.AccountRole;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = LedgerAccountController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class LedgerAccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ListLedgerAccountsByPropertyUseCase listLedgerAccountsByPropertyUseCase;

    @MockitoBean
    private ListJournalEntriesByTreasuryAccountUseCase listJournalEntriesByTreasuryAccountUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/ledger-accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_list_a_property_s_ledger_accounts() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(listLedgerAccountsByPropertyUseCase.list(any())).thenReturn(List.of(
                new LedgerAccountView(LedgerAccountId.newId(), propertyId, null, "51610001", "Caisse", 5,
                        AccountNature.BALANCE_ASSET, EntryDirection.DEBIT, false, AccountRole.CASH, true,
                        BigDecimal.ZERO, null)));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/accounting/ledger-accounts")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_listing_ledger_accounts() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/ledger-accounts")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isForbidden());
    }
}
