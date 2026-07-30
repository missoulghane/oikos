package com.architek.oikos.installment.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.installment.application.dto.InstallmentView;
import com.architek.oikos.installment.application.port.in.GetInstallmentUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentsByUnitUseCase;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.installment.domain.valueobject.InstallmentStatus;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = InstallmentController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class InstallmentControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private ListInstallmentsByUnitUseCase listInstallmentsByUnitUseCase;

    @MockitoBean
    private ListInstallmentsByPropertyUseCase listInstallmentsByPropertyUseCase;

    @MockitoBean
    private GetInstallmentUseCase getInstallmentUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/units/" + EntityId.newId() + "/installments")).andExpect(status().isUnauthorized());
    }

    @Test
    void lists_installments_of_a_unit() throws Exception {
        when(listInstallmentsByUnitUseCase.listInstallments(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/units/" + EntityId.newId() + "/installments")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void lists_installments_of_a_property() throws Exception {
        when(listInstallmentsByPropertyUseCase.listInstallments(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + EntityId.newId() + "/installments")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void lists_installments_of_a_property_with_filters_sort_and_pagination() throws Exception {
        when(listInstallmentsByPropertyUseCase.listInstallments(any())).thenReturn(Page.of(List.of(), 1, 5, 0));

        mockMvc.perform(get("/api/v1/properties/" + EntityId.newId() + "/installments")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "AMOUNT")
                        .param("sortDirection", "DESC")
                        .param("status", "PARTIALLY_SETTLED", "NOT_SETTLED")
                        .param("dueDateFrom", "2026-01-01")
                        .param("dueDateTo", "2026-12-31")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void gets_an_installment_by_id() throws Exception {
        InstallmentId id = InstallmentId.newId();
        when(getInstallmentUseCase.getInstallment(any())).thenReturn(new InstallmentView(id,
                EntityId.newId(), LocalDate.of(2027, 1, 1), new BigDecimal("250"), new BigDecimal("250"),
                InstallmentStatus.NOT_SETTLED));

        mockMvc.perform(get("/api/v1/installments/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }
}
