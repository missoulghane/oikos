package com.architek.oikos.installment.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.architek.oikos.installment.domain.valueobject.PaymentMode;

/**
 * Everything the receipt lays out, resolved once by GeneratePaymentReceiptService.
 * Values stay typed (amounts, dates, the mode enum): turning them into French
 * text is presentation, and belongs to the renderer, not here.
 *
 * payerNames may be empty - a lot with no owner attached is unusual but legal
 * (OwnershipStatus.NOT_AFFECTED), and a receipt must still be produced for it.
 */
public record PaymentReceiptView(String receiptNumber, String propertyName, String unitNumber,
                                  List<String> payerNames, LocalDate valueDate, PaymentMode mode,
                                  BigDecimal amount, BigDecimal imputedAmount, BigDecimal advanceAmount,
                                  int allocationCount) {
}
