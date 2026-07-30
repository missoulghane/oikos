package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.port.in.GetOpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.OpenAccountingExerciseUseCase;
import com.architek.oikos.accounting.domain.valueobject.AccountingExerciseId;
import com.architek.oikos.accounting.domain.valueobject.ExerciseStatus;
import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = AccountingExerciseController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class AccountingExerciseControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private OpenAccountingExerciseUseCase openAccountingExerciseUseCase;

    @MockitoBean
    private GetOpenAccountingExerciseUseCase getOpenAccountingExerciseUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/exercises/open"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void admin_can_open_an_exercise() throws Exception {
        when(openAccountingExerciseUseCase.open(any())).thenReturn(AccountingExerciseId.newId());

        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/exercises")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label":"Exercice 2026","startDate":"2026-01-01","endDate":"2026-12-31"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void admin_can_read_the_open_exercise() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(getOpenAccountingExerciseUseCase.get(any())).thenReturn(new AccountingExerciseView(
                AccountingExerciseId.newId(), propertyId, "Exercice 2026", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), ExerciseStatus.OPEN, null, null, null));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/accounting/exercises/open")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void regular_user_is_forbidden_from_opening_an_exercise() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + UUID.randomUUID() + "/accounting/exercises")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"label":"Exercice 2026","startDate":"2026-01-01","endDate":"2026-12-31"}
                                """))
                .andExpect(status().isForbidden());
    }
}
