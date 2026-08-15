package com.architek.oikos.installment.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.document.application.command.UploadDocumentCommand;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.DeleteDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.port.in.UploadDocumentUseCase;
import com.architek.oikos.document.application.command.DeleteDocumentCommand;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.installment.application.dto.PaymentReceiptView;
import com.architek.oikos.installment.application.dto.PaymentView;
import com.architek.oikos.installment.application.port.in.GeneratePaymentReceiptUseCase;
import com.architek.oikos.installment.application.port.in.GetPaymentUseCase;
import com.architek.oikos.installment.application.port.out.PaymentReceiptRendererPort;
import com.architek.oikos.installment.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.installment.application.port.out.UnitDirectoryPort;
import com.architek.oikos.installment.application.query.GetPaymentQuery;
import com.architek.oikos.installment.domain.model.Payment;
import com.architek.oikos.installment.domain.repository.PaymentRepository;
import com.architek.oikos.installment.domain.repository.ReceiptNumberSequenceRepository;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Builds the receipt of a payment and stores it as a Document owned by that
 * payment. Reuses the document module wholesale (storage, checksum, download)
 * rather than adding a second way to keep a file - hence no new column and no
 * migration for the attachment itself.
 */
@Component
public class GeneratePaymentReceiptService implements GeneratePaymentReceiptUseCase {

    private static final Logger log = LoggerFactory.getLogger(GeneratePaymentReceiptService.class);

    /** A payment owns at most one receipt; the page size only has to cover that. */
    private static final int EXISTING_RECEIPTS_PAGE_SIZE = 50;

    private final GetPaymentUseCase getPaymentUseCase;
    private final PaymentRepository paymentRepository;
    private final ReceiptNumberSequenceRepository receiptNumberSequenceRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final UnitDirectoryPort unitDirectoryPort;
    private final PaymentReceiptRendererPort rendererPort;
    private final UploadDocumentUseCase uploadDocumentUseCase;
    private final ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;

    public GeneratePaymentReceiptService(GetPaymentUseCase getPaymentUseCase, PaymentRepository paymentRepository,
                                          ReceiptNumberSequenceRepository receiptNumberSequenceRepository,
                                          PropertyDirectoryPort propertyDirectoryPort,
                                          UnitDirectoryPort unitDirectoryPort,
                                          PaymentReceiptRendererPort rendererPort,
                                          UploadDocumentUseCase uploadDocumentUseCase,
                                          ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase,
                                          DeleteDocumentUseCase deleteDocumentUseCase) {
        this.getPaymentUseCase = getPaymentUseCase;
        this.paymentRepository = paymentRepository;
        this.receiptNumberSequenceRepository = receiptNumberSequenceRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.unitDirectoryPort = unitDirectoryPort;
        this.rendererPort = rendererPort;
        this.uploadDocumentUseCase = uploadDocumentUseCase;
        this.listDocumentsByOwnerUseCase = listDocumentsByOwnerUseCase;
        this.deleteDocumentUseCase = deleteDocumentUseCase;
    }

    /**
     * REQUIRES_NEW, and not the default REQUIRED: this runs from an AFTER_COMMIT
     * listener, where the original transaction is completing but its
     * synchronization is still bound to the thread. A REQUIRED transaction joins
     * that dying context and everything written here is discarded on the way out -
     * silently, with no exception to catch and no row to find afterwards. The
     * download then answers 404 for a payment that looked perfectly recorded.
     * Reproduced by PaymentReceiptGenerationIntegrationTest.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generate(PaymentId paymentId, EntityId generatedByUserId) {
        PaymentView payment = getPaymentUseCase.getPayment(new GetPaymentQuery(paymentId));
        ReceiptNumber receiptNumber = resolveReceiptNumber(payment);

        PaymentReceiptView receipt = new PaymentReceiptView(receiptNumber.format(),
                propertyDirectoryPort.getName(payment.propertyId()),
                unitDirectoryPort.getUnitNumber(payment.unitId()),
                unitDirectoryPort.getOwnerFullNames(payment.unitId()),
                payment.valueDate(), payment.mode(), payment.amount(),
                imputedAmountOf(payment), advanceAmountOf(payment), 0);

        // Replaced, not added to: the document module rejects a byte-identical
        // upload for the same owner (uk_document_owner_checksum), so a
        // regeneration that changed nothing would otherwise fail as a duplicate.
        deleteExistingReceipts(paymentId);

        // Last argument is uploadedBy, not the property: document.uploaded_by is a
        // foreign key onto app_user, so anything else violates it at flush time -
        // invisible in the H2 test schema, fatal on Postgres.
        uploadDocumentUseCase.upload(new UploadDocumentCommand(DocumentOwnerType.PAYMENT,
                EntityId.of(paymentId.asUuid()),
                "recu-" + receiptNumber.format() + ".pdf", "application/pdf", rendererPort.render(receipt),
                generatedByUserId));
        log.info("Receipt {} generated for payment {}", receiptNumber.format(), paymentId);
    }

    /**
     * Payments predating receipt numbering carry none; one is allocated on first
     * generation and persisted, so a reprint keeps the same reference.
     */
    private ReceiptNumber resolveReceiptNumber(PaymentView payment) {
        if (payment.receiptNumber() != null) {
            return ReceiptNumber.parse(payment.receiptNumber());
        }
        ReceiptNumber allocated = receiptNumberSequenceRepository.allocate(payment.propertyId(),
                payment.valueDate().getYear());
        Payment stored = paymentRepository.findById(payment.id()).orElseThrow();
        paymentRepository.save(stored.withReceiptNumber(allocated));
        return allocated;
    }

    private void deleteExistingReceipts(PaymentId paymentId) {
        List<DocumentView> existing = listDocumentsByOwnerUseCase.list(
                new ListDocumentsByOwnerQuery(DocumentOwnerType.PAYMENT, EntityId.of(paymentId.asUuid()),
                        PageRequest.of(0, EXISTING_RECEIPTS_PAGE_SIZE))).content();
        for (DocumentView document : existing) {
            deleteDocumentUseCase.delete(new DeleteDocumentCommand(document.id()));
        }
    }

    /**
     * The imputation split is only returned when the payment is created, never
     * stored, so a receipt regenerated later cannot restate it. Left at zero
     * rather than guessed - the template hides the breakdown entirely then.
     */
    private BigDecimal imputedAmountOf(PaymentView payment) {
        return BigDecimal.ZERO;
    }

    private BigDecimal advanceAmountOf(PaymentView payment) {
        return BigDecimal.ZERO;
    }
}
