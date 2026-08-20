package com.architek.oikos.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.installment.application.event.PaymentRecordedEvent;
import com.architek.oikos.installment.application.port.in.GeneratePaymentReceiptUseCase;
import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.repository.PaymentRepository;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;
import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.dto.UnitView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.port.in.GetUnitUseCase;
import com.architek.oikos.property.application.port.in.ListUnitOwnershipsByUnitUseCase;
import com.architek.oikos.property.domain.valueobject.BuildingId;
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.OwnershipStatus;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.UnitId;
import com.architek.oikos.property.domain.valueobject.UnitTypeDefinitionId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.Amount;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The receipt is generated from a @TransactionalEventListener(AFTER_COMMIT), and
 * that phase has a trap: the transaction is completing but synchronization is
 * still active, so what the listener writes can silently fail to commit. The
 * download then 404s with no error anywhere - exactly the symptom reported.
 *
 * These two tests separate the two halves: does generation work at all, and does
 * it survive being driven from the after-commit phase.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentReceiptGenerationIntegrationTest {

    @Autowired
    private GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // Mocked at the property module's port-ins rather than at installment's
    // directory ports: the adapter behind those implements three ports at once,
    // so replacing it by type breaks the beans expecting the other two.
    @MockitoBean
    private GetPropertyUseCase getPropertyUseCase;

    @MockitoBean
    private GetUnitUseCase getUnitUseCase;

    @MockitoBean
    private ListUnitOwnershipsByUnitUseCase listUnitOwnershipsByUnitUseCase;

    @BeforeEach
    void stubDirectories() {
        when(getPropertyUseCase.getProperty(any())).thenReturn(new PropertyView(PropertyId.newId(),
                "Résidence Al Amal", "12 rue Exemple", null, DuesCalculationMode.FLAT_RATE, null));
        when(getUnitUseCase.getUnit(any())).thenReturn(new UnitView(UnitId.newId(), BuildingId.newId(),
                PropertyId.newId(), "A12", UnitTypeDefinitionId.newId(), "Appartement", new BigDecimal("120.00"),
                OwnershipStatus.AFFECTED, List.of()));
        when(listUnitOwnershipsByUnitUseCase.listUnitOwnerships(any())).thenReturn(List.of());
    }

    /**
     * Stands for the syndic who records the payment. It matters that this is a
     * user id and nothing else: it lands in document.uploaded_by, a foreign key
     * onto app_user on Postgres - which the H2 schema built from the entities
     * does not carry, so only the assertion below guards it.
     */
    private static final EntityId RECORDING_USER = EntityId.newId();

    private PaymentId givenAPayment() {
        Payment payment = Payment.create(PaymentId.newId(), EntityId.newId(), EntityId.newId(), PaymentMode.CHECK,
                LocalDate.of(2026, 3, 15), Amount.of(new BigDecimal("2500.00")), EntityId.newId(),
                new ReceiptNumber(2026, 42));
        return paymentRepository.save(payment).getId();
    }

    private List<DocumentView> receiptsOf(PaymentId paymentId) {
        return listDocumentsByOwnerUseCase.list(new ListDocumentsByOwnerQuery(DocumentOwnerType.PAYMENT,
                EntityId.of(paymentId.asUuid()), PageRequest.of(0, 10))).content();
    }

    @Test
    void generating_a_receipt_attaches_it_to_the_payment() {
        PaymentId paymentId = givenAPayment();

        generatePaymentReceiptUseCase.generate(paymentId, RECORDING_USER);

        assertThat(receiptsOf(paymentId)).hasSize(1);
    }

    /**
     * The receipt was attributed to the payment's property instead of to a user,
     * and document.uploaded_by references app_user: every generation failed on
     * Postgres with a foreign key violation, while passing here.
     */
    @Test
    void the_receipt_is_attributed_to_the_user_who_generated_it() {
        PaymentId paymentId = givenAPayment();

        generatePaymentReceiptUseCase.generate(paymentId, RECORDING_USER);

        assertThat(receiptsOf(paymentId).getFirst().uploadedBy())
                .as("uploaded_by is a foreign key onto app_user - a property id violates it")
                .isEqualTo(RECORDING_USER);
    }

    /**
     * The production path. If the after-commit write is lost, this fails while
     * the test above passes - which is precisely the shape of the bug.
     */
    @Test
    void a_receipt_published_after_commit_is_actually_persisted() {
        PaymentId paymentId = transactionTemplate.execute(status -> {
            PaymentId id = givenAPayment();
            eventPublisher.publishEvent(new PaymentRecordedEvent(id, RECORDING_USER));
            return id;
        });

        assertThat(receiptsOf(paymentId))
                .as("the receipt written from the AFTER_COMMIT listener must be committed, not silently dropped")
                .hasSize(1);
    }

    @Test
    void regenerating_replaces_the_receipt_rather_than_adding_a_second() {
        PaymentId paymentId = givenAPayment();

        generatePaymentReceiptUseCase.generate(paymentId, RECORDING_USER);
        generatePaymentReceiptUseCase.generate(paymentId, RECORDING_USER);

        assertThat(receiptsOf(paymentId)).hasSize(1);
    }
}
