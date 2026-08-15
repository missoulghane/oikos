package com.architek.oikos.installment.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.document.application.dto.DocumentContentView;
import com.architek.oikos.document.application.dto.DocumentView;
import com.architek.oikos.document.application.port.in.DownloadDocumentUseCase;
import com.architek.oikos.document.application.port.in.ListDocumentsByOwnerUseCase;
import com.architek.oikos.document.application.query.GetDocumentQuery;
import com.architek.oikos.document.application.query.ListDocumentsByOwnerQuery;
import com.architek.oikos.document.domain.valueobject.DocumentOwnerType;
import com.architek.oikos.installment.application.command.RecordOwnerPaymentCommand;
import com.architek.oikos.installment.application.port.in.GeneratePaymentReceiptUseCase;
import com.architek.oikos.installment.application.port.in.GetLatestPaymentByPropertyUseCase;
import com.architek.oikos.installment.application.port.in.ListPaymentsByUnitUseCase;
import com.architek.oikos.installment.application.port.in.RecordOwnerPaymentUseCase;
import com.architek.oikos.installment.application.query.GetLatestPaymentByPropertyQuery;
import com.architek.oikos.installment.application.query.ListPaymentsByUnitQuery;
import com.architek.oikos.installment.web.request.RecordOwnerPaymentRequest;
import com.architek.oikos.installment.web.response.PaymentResponse;
import com.architek.oikos.installment.web.response.RecordOwnerPaymentResponse;
import com.architek.oikos.installment.domain.exception.PaymentReceiptNotFoundException;
import com.architek.oikos.installment.domain.valueobject.PaymentId;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@RestController
public class PaymentController {

    private final GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase;
    private final DownloadDocumentUseCase downloadDocumentUseCase;
    private final ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase;

    private final RecordOwnerPaymentUseCase recordOwnerPaymentUseCase;
    private final ListPaymentsByUnitUseCase listPaymentsByUnitUseCase;
    private final GetLatestPaymentByPropertyUseCase getLatestPaymentByPropertyUseCase;

    public PaymentController(RecordOwnerPaymentUseCase recordOwnerPaymentUseCase,
                              ListPaymentsByUnitUseCase listPaymentsByUnitUseCase,
                              GetLatestPaymentByPropertyUseCase getLatestPaymentByPropertyUseCase,
                              GeneratePaymentReceiptUseCase generatePaymentReceiptUseCase,
                              DownloadDocumentUseCase downloadDocumentUseCase,
                              ListDocumentsByOwnerUseCase listDocumentsByOwnerUseCase) {
        this.recordOwnerPaymentUseCase = recordOwnerPaymentUseCase;
        this.listPaymentsByUnitUseCase = listPaymentsByUnitUseCase;
        this.getLatestPaymentByPropertyUseCase = getLatestPaymentByPropertyUseCase;
        this.generatePaymentReceiptUseCase = generatePaymentReceiptUseCase;
        this.downloadDocumentUseCase = downloadDocumentUseCase;
        this.listDocumentsByOwnerUseCase = listDocumentsByOwnerUseCase;
    }

    @PreAuthorize("@propertyAccess.canWriteInstallmentCall(authentication, #propertyId)")
    @PostMapping("/properties/{propertyId}/units/{unitId}/payments")
    public ResponseEntity<RecordOwnerPaymentResponse> record(@PathVariable String propertyId,
                                                              @PathVariable String unitId,
                                                              @Valid @RequestBody RecordOwnerPaymentRequest request,
                                                              Authentication authentication) {
        RecordOwnerPaymentResponse response = RecordOwnerPaymentResponse.from(recordOwnerPaymentUseCase.record(
                new RecordOwnerPaymentCommand(EntityId.of(propertyId), EntityId.of(unitId), request.mode(),
                        EntityId.of(request.treasuryAccountId()), request.valueDate(), request.amount(),
                        EntityId.of(authentication.getName()))));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("@propertyAccess.managesUnit(authentication, #unitId) or @propertyAccess.ownsUnit(authentication, #unitId)")
    @GetMapping("/units/{unitId}/payments")
    public List<PaymentResponse> listByUnit(@PathVariable String unitId) {
        return listPaymentsByUnitUseCase.listPayments(new ListPaymentsByUnitQuery(EntityId.of(unitId))).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    /**
     * The receipt of a single payment. Guarded by managesPayment, not by the
     * generic document permission: a receipt names one owner and states what
     * they paid, and every copropriétaire holds DOCUMENT_READ on their property -
     * the permission path would let any of them read any other's receipt.
     */
    @PreAuthorize("@propertyAccess.managesPayment(authentication, #paymentId)")
    @GetMapping("/payments/{paymentId}/receipt")
    public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable String paymentId) {
        DocumentView receipt = receiptDocumentOf(paymentId)
                .orElseThrow(() -> new PaymentReceiptNotFoundException(paymentId));
        DocumentContentView content = downloadDocumentUseCase.download(new GetDocumentQuery(receipt.id()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(content.fileName()).build().toString())
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.sizeBytes())
                .body(new ByteArrayResource(content.content()));
    }

    /** Regenerates and replaces the receipt - for a payment whose generation failed, or predates the feature. */
    @PreAuthorize("@propertyAccess.managesPayment(authentication, #paymentId)")
    @PostMapping("/payments/{paymentId}/receipt")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void regenerateReceipt(@PathVariable String paymentId, Authentication authentication) {
        generatePaymentReceiptUseCase.generate(PaymentId.of(paymentId), EntityId.of(authentication.getName()));
    }

    private java.util.Optional<DocumentView> receiptDocumentOf(String paymentId) {
        return listDocumentsByOwnerUseCase.list(new ListDocumentsByOwnerQuery(DocumentOwnerType.PAYMENT,
                        EntityId.of(paymentId), PageRequest.of(0, 1)))
                .content().stream().findFirst();
    }

    @PreAuthorize("@propertyAccess.canReadAccounting(authentication, #propertyId)")
    @GetMapping("/properties/{propertyId}/payments/latest")
    public List<PaymentResponse> latestForProperty(@PathVariable String propertyId,
                                                    @RequestParam(defaultValue = "5") int size) {
        return getLatestPaymentByPropertyUseCase
                .getLatestPayment(new GetLatestPaymentByPropertyQuery(EntityId.of(propertyId), size))
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }
}
