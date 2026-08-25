package com.architek.oikos.installment.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.GeneratePaymentReceiptUseCase;
import com.architek.oikos.installment.application.port.in.GetLatestPaymentByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.port.in.ListPaymentsByUnitUseCase;
import com.architek.oikos.installment.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.property.application.dto.UnitOwnershipView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.property.domain.valueobject.UnitOwnershipId;
import com.architek.oikos.shared.domain.pagination.Page;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.testsupport.WebSecuritySliceTestConfiguration;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;

/**
 * The confidentiality rule of a receipt, which is the whole reason
 * managesPayment exists: a receipt names one owner and states what they paid,
 * and every copropriétaire holds DOCUMENT_READ on their property. Going through
 * the generic document permission would let any of them read any other's.
 */
@WebMvcTest(controllers = PaymentController.class)
@Import(WebSecuritySliceTestConfiguration.class)
class PaymentReceiptControllerWebMvcTest {

    private static final EntityId PROPERTY = EntityId.newId();
    private static final EntityId PAID_UNIT = EntityId.newId();
    private static final PaymentId PAYMENT = PaymentId.newId();

    private static final String OWNER_PARTY = UUID.randomUUID().toString();
    private static final String OTHER_PARTY = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private RecordOwnerPaymentUseCase recordOwnerPaymentUseCase;

    @MockitoBean
    private ListPaymentsByUnitUseCase listPaymentsByUnitUseCase;

    @MockitoBean
    private GetLatestPaymentByPropertyUseCase getLatestPaymentByPropertyUseCase;

    @MockitoBean
    private GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase;

    @MockitoBean
    private DownloadDocumentUseCase downloadDocumentUseCase;

    @MockitoBean
    private ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;

    @MockitoBean
    private GetPaymentUseCase getPaymentUseCase;

    @MockitoBean
    private GetUserAccessUseCase getUserAccessUseCase;

    @MockitoBean
    private ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;

    // managesUnit resolves the lot to its property through this one; left
    // unstubbed the slice's bare mock returns null and every call 500s.
    @MockitoBean
    private GetUnitUseCase getUnitUseCase;

    private String bearerFor(String partyId) {
        when(getUserAccessUseCase.getAccess(any())).thenReturn(new UserAccessView(Set.of(),
                java.util.Map.of(), java.util.Map.of(), Set.of(), Set.of(partyId)));
        return "Bearer " + jwtService.generateAccessToken(EntityId.of(UUID.randomUUID()), Set.of("ROLE_USER"));
    }

    private void givenPaymentOnUnitOwnedBy(String partyId) {
        when(getUnitUseCase.getUnit(any())).thenReturn(new UnitView(UnitId.of(PAID_UNIT.value()),
                BuildingId.newId(), PropertyId.of(PROPERTY.value()), "A12", UnitTypeDefinitionId.newId(),
                "Appartement", new BigDecimal("120.00"), OwnershipStatus.AFFECTED, List.of(), null));
        when(listDocumentsByOwnerUseCase.list(any())).thenReturn(Page.of(List.of(), 0, 1, 0));
        when(getPaymentUseCase.getPayment(any())).thenReturn(new PaymentView(PAYMENT, PROPERTY, PAID_UNIT,
                PaymentMode.CHECK, LocalDate.of(2026, 3, 15), new BigDecimal("2500.00"), EntityId.newId(),
                "REC-2026-0042"));
        when(listUnitOwnershipsByUnitUseCase.listUnitOwnerships(any())).thenReturn(List.of(
                new UnitOwnershipView(UnitOwnershipId.newId(), UnitId.of(PAID_UNIT.value()), EntityId.of(partyId),
                        "Rachid Tazi", PartyType.INDIVIDUAL, "user5@oikos.com", new BigDecimal("100.00"))));
    }

    @Test
    void the_owner_of_the_paid_lot_may_download_their_receipt() throws Exception {
        givenPaymentOnUnitOwnedBy(OWNER_PARTY);

        // 404, not 403: authorization passed and the controller then looked for a
        // receipt this test deliberately did not stub.
        mockMvc.perform(get("/api/v1/payments/" + PAYMENT + "/receipt").header("Authorization", bearerFor(OWNER_PARTY)))
                .andExpect(status().isNotFound());
    }

    @Test
    void a_copropietaire_of_another_lot_is_refused() throws Exception {
        givenPaymentOnUnitOwnedBy(OWNER_PARTY);

        mockMvc.perform(get("/api/v1/payments/" + PAYMENT + "/receipt").header("Authorization", bearerFor(OTHER_PARTY)))
                .andExpect(status().isForbidden());

        verify(downloadDocumentUseCase, never()).download(any());
    }

    @Test
    void anonymous_request_is_rejected() throws Exception {
        mockMvc.perform(get("/api/v1/payments/" + PAYMENT + "/receipt")).andExpect(status().isUnauthorized());
    }
}
