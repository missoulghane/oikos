package com.architek.oikos.installment.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.YearMonth;
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
import com.architek.oikos.installment.application.dto.InstallmentCallDetailView;
import com.architek.oikos.installment.application.dto.InstallmentCallView;
import com.architek.oikos.installment.application.dto.GenerateInstallmentCallResult;
import com.architek.oikos.installment.application.port.in.GenerateInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.GetInstallmentCallUseCase;
import com.architek.oikos.installment.application.port.in.ListInstallmentCallsByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.RecordInstallmentCallUseCase;
import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = InstallmentCallController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class InstallmentCallControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RecordInstallmentCallUseCase recordInstallmentCallUseCase;

    @MockitoBean
    private GenerateInstallmentCallUseCase generateInstallmentCallUseCase;

    @MockitoBean
    private ListInstallmentCallsByPropertyUseCase listInstallmentCallsByPropertyUseCase;

    @MockitoBean
    private GetInstallmentCallUseCase getInstallmentCallUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(post("/api/v1/installment-calls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dueDate":"2027-01-01","lines":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void records_a_installment_call() throws Exception {
        when(recordInstallmentCallUseCase.record(any())).thenReturn(List.of(InstallmentId.newId()));

        mockMvc.perform(post("/api/v1/installment-calls")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dueDate":"2027-01-01","lines":[{"unitId":"%s","amount":250}]}
                                """.formatted(EntityId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void a_installment_call_without_any_line_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/installment-calls")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"dueDate":"2027-01-01","lines":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generates_a_installment_call_for_a_property() throws Exception {
        EntityId propertyId = EntityId.newId();
        InstallmentCallView view = new InstallmentCallView(InstallmentCallId.newId(), propertyId, YearMonth.of(2026, 1),
                LocalDate.of(2026, 2, 5));
        when(generateInstallmentCallUseCase.generate(any()))
                .thenReturn(new GenerateInstallmentCallResult(view, List.of(EntityId.newId()), List.of()));

        mockMvc.perform(post("/api/v1/properties/" + propertyId + "/installment-calls")
                        .header("Authorization", bearerToken("ROLE_ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"period":"2026-01","dueDate":"2026-02-05"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void generating_with_a_malformed_period_returns_400() throws Exception {
        mockMvc.perform(post("/api/v1/properties/" + EntityId.newId() + "/installment-calls")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"period":"not-a-period","dueDate":"2026-02-05"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void lists_installment_calls_for_a_property() throws Exception {
        when(listInstallmentCallsByPropertyUseCase.listInstallmentCalls(any())).thenReturn(Page.of(List.of(), 0, 20, 0));

        mockMvc.perform(get("/api/v1/properties/" + EntityId.newId() + "/installment-calls")
                        .header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void gets_a_installment_call_by_id() throws Exception {
        InstallmentCallId id = InstallmentCallId.newId();
        InstallmentCallView view = new InstallmentCallView(id, EntityId.newId(), YearMonth.of(2026, 1), LocalDate.of(2026, 2, 5));
        when(getInstallmentCallUseCase.getInstallmentCall(any())).thenReturn(new InstallmentCallDetailView(view, List.of()));

        mockMvc.perform(get("/api/v1/installment-calls/" + id).header("Authorization", bearerToken("ROLE_ADMIN")))
                .andExpect(status().isOk());
    }
}
