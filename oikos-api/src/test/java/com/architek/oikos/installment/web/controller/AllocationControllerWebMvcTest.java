package com.architek.oikos.installment.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

import com.architek.oikos.auth.infrastructure.security.JwtService;
import com.architek.oikos.installment.application.port.in.AllocatePaymentUseCase;
import com.architek.oikos.installment.application.port.in.DeallocateUseCase;
import com.architek.oikos.installment.application.port.in.ListAllocationsByMovementUseCase;
import com.architek.oikos.installment.domain.valueobject.AllocationId;
import com.architek.oikos.installment.domain.valueobject.InstallmentId;
import com.architek.oikos.accounting.domain.valueobject.MovementId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;

@WebMvcTest(controllers = AllocationController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class AllocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AllocatePaymentUseCase allocatePaymentUseCase;

    @MockitoBean
    private DeallocateUseCase deallocateUseCase;

    @MockitoBean
    private ListAllocationsByMovementUseCase listAllocationsByMovementUseCase;

    private String bearerToken(String... authorities) {
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of(authorities));
    }

    @Test
    void anonymous_request_is_rejected_with_401() throws Exception {
        mockMvc.perform(get("/api/v1/movements/" + MovementId.newId() + "/allocations")).andExpect(status().isUnauthorized());
    }

    @Test
    void allocates_a_payment_to_an_installment() throws Exception {
        when(allocatePaymentUseCase.allocate(any())).thenReturn(AllocationId.newId());

        mockMvc.perform(post("/api/v1/allocations")
                        .header("Authorization", bearerToken("ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"movementId":"%s","installmentId":"%s","amount":100}
                                """.formatted(MovementId.newId(), InstallmentId.newId())))
                .andExpect(status().isCreated());
    }

    @Test
    void deallocates_an_allocation() throws Exception {
        mockMvc.perform(delete("/api/v1/allocations/" + AllocationId.newId())
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    void lists_allocations_of_a_movement() throws Exception {
        when(listAllocationsByMovementUseCase.listAllocations(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/movements/" + MovementId.newId() + "/allocations")
                        .header("Authorization", bearerToken("ROLE_USER")))
                .andExpect(status().isOk());
    }
}
