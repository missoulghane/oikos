package com.architek.oikos.accounting.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

import com.architek.oikos.accounting.application.dto.AccountingExerciseView;
import com.architek.oikos.accounting.application.dto.ExerciseClosingView;
import com.architek.oikos.accounting.application.port.in.CloseAccountingExerciseUseCase;
import com.architek.oikos.accounting.application.port.in.ListAccountingExercisesUseCase;
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
    private ListAccountingExercisesUseCase listAccountingExercisesUseCase;

    @MockitoBean
    private CloseAccountingExerciseUseCase closeAccountingExerciseUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/properties/" + UUID.randomUUID() + "/accounting/exercises"))
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
    void admin_can_list_the_exercises_of_a_property() throws Exception {
        EntityId propertyId = EntityId.newId();
        when(listAccountingExercisesUseCase.list(any())).thenReturn(List.of(
                exercise(propertyId, 2026, ExerciseStatus.OPEN), exercise(propertyId, 2027, ExerciseStatus.OPEN)));

        mockMvc.perform(get("/api/v1/properties/" + propertyId + "/accounting/exercises")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].label").value("Exercice 2027"));
    }

    @Test
    void admin_can_close_a_designated_exercise() throws Exception {
        EntityId propertyId = EntityId.newId();
        AccountingExerciseView closed = exercise(propertyId, 2026, ExerciseStatus.CLOSED);
        when(closeAccountingExerciseUseCase.close(any()))
                .thenReturn(new ExerciseClosingView(closed, BigDecimal.ZERO, null));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/accounting/exercises/" + closed.id() + "/close")
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

    private static AccountingExerciseView exercise(EntityId propertyId, int year, ExerciseStatus status) {
        return new AccountingExerciseView(AccountingExerciseId.newId(), propertyId, "Exercice " + year,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), status, null, null, null);
    }
}
