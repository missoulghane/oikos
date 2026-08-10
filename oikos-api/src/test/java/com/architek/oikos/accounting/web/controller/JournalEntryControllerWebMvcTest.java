package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
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

import com.architek.oikos.accounting.application.dto.JournalEntryLineView;
import com.architek.oikos.accounting.application.dto.JournalEntryView;
import com.architek.oikos.accounting.application.port.in.CreateJournalEntryDraftUseCase;
import com.architek.oikos.accounting.application.port.in.GetJournalEntryUseCase;
import com.architek.oikos.accounting.application.port.in.ListJournalEntriesByPropertyUseCase;
import com.architek.oikos.accounting.application.port.in.PostJournalEntryUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.EntryDirection;
import com.architek.oikos.accounting.domain.valueobject.JournalCode;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryLineId;
import com.architek.oikos.accounting.domain.valueobject.JournalEntryStatus;
import com.architek.oikos.accounting.domain.valueobject.LedgerAccountId;
import com.architek.oikos.accounting.domain.valueobject.PeriodId;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = JournalEntryController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class JournalEntryControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private CreateJournalEntryDraftUseCase createJournalEntryDraftUseCase;

    @MockitoBean
    private PostJournalEntryUseCase postJournalEntryUseCase;

    @MockitoBean
    private GetJournalEntryUseCase getJournalEntryUseCase;

    @MockitoBean
    private ListJournalEntriesByPropertyUseCase listJournalEntriesByPropertyUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    private JournalEntryView sampleView(EntityId propertyId) {
        JournalEntryLineView line = new JournalEntryLineView(JournalEntryLineId.newId(), LedgerAccountId.newId(),
                null, null, EntryDirection.DEBIT, new BigDecimal("100.00"), "Debit");
        return new JournalEntryView(JournalEntryId.newId(), propertyId, AccountingExerciseId.newId(), PeriodId.newId(),
                JournalCode.OD, null, LocalDate.of(2026, 8, 1), null, null, JournalEntryStatus.DRAFT, null,
                EntityId.newId(), List.of(line, line));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/entries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_create_a_draft_entry() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(createJournalEntryDraftUseCase.create(any())).thenReturn(JournalEntryId.newId());

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/accounting/entries")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"journalCode":"OD","pieceDate":"2026-08-01","lines":[
                                  {"ledgerAccountId":"%s","direction":"DEBIT","amount":100.00,"label":"Debit"},
                                  {"ledgerAccountId":"%s","direction":"CREDIT","amount":100.00,"label":"Credit"}
                                ]}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_read_an_entry() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(getJournalEntryUseCase.get(any())).thenReturn(sampleView(propertyId));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/accounting/entries/" + UUID.randomUUID())
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_list_entries() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(listJournalEntriesByPropertyUseCase.list(any())).thenReturn(Page.of(List.of(sampleView(propertyId)), 0, 20, 1));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/accounting/entries")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void admin_can_validate_an_entry() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(postJournalEntryUseCase.post(any())).thenReturn(sampleView(propertyId));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/accounting/entries/" + UUID.randomUUID() + "/validation")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_creating_an_entry() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/entries")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"journalCode":"OD","pieceDate":"2026-08-01","lines":[
                                  {"ledgerAccountId":"%s","direction":"DEBIT","amount":100.00,"label":"Debit"},
                                  {"ledgerAccountId":"%s","direction":"CREDIT","amount":100.00,"label":"Credit"}
                                ]}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }
}
