package com.architek.oikos.installment.infrastructure.receipt;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;
import com.architek.oikos.installment.application.dto.PaymentReceiptView;
import com.architek.oikos.installment.application.port.out.PaymentReceiptRendererPort;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;

/**
 * Renders the receipt template to HTML (Thymeleaf), then that HTML to a PDF
 * (openhtmltopdf / PDFBox). Both the templating engine and the PDF library stay
 * behind PaymentReceiptRendererPort so no other layer ever imports them.
 *
 * All French formatting happens here rather than in the view: how an amount or a
 * date reads is presentation.
 */
@Component
@RequiredArgsConstructor
public class ThymeleafPaymentReceiptRenderer implements PaymentReceiptRendererPort {

    private static final String TEMPLATE = "receipt/payment-receipt";
    private static final Locale FR = Locale.FRANCE;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", FR);

    private static final java.util.Map<PaymentMode, String> MODE_LABELS = java.util.Map.of(
            PaymentMode.BANK_TRANSFER, "Virement",
            PaymentMode.CASH, "Espèces",
            PaymentMode.CHECK, "Chèque",
            PaymentMode.DIRECT_DEBIT, "Prélèvement");

    private final ITemplateEngine templateEngine;

    @Override
    public byte[] render(PaymentReceiptView receipt) {
        Context context = new Context(FR);
        context.setVariable("receiptNumber", receipt.receiptNumber());
        context.setVariable("issuedOn", DATE.format(LocalDate.now()));
        context.setVariable("propertyName", receipt.propertyName());
        context.setVariable("unitLabel", "Lot " + receipt.unitNumber());
        context.setVariable("payerName", receipt.payerNames().isEmpty() ? null : String.join(", ", receipt.payerNames()));
        context.setVariable("valueDate", DATE.format(receipt.valueDate()));
        context.setVariable("paymentMode", MODE_LABELS.getOrDefault(receipt.mode(), receipt.mode().name()));
        context.setVariable("amount", money(receipt.amount()));

        boolean hasImputed = isPositive(receipt.imputedAmount());
        boolean hasAdvance = isPositive(receipt.advanceAmount());
        context.setVariable("hasBreakdown", hasImputed || hasAdvance);
        context.setVariable("imputedAmount", hasImputed ? money(receipt.imputedAmount()) : null);
        context.setVariable("advanceAmount", hasAdvance ? money(receipt.advanceAmount()) : null);
        context.setVariable("allocationCount", receipt.allocationCount());

        String html = templateEngine.process(TEMPLATE, context);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        // No base URI to resolve against: the template embeds its own CSS and
        // pulls no external asset, so nothing has to be fetched at render time.
        builder.withHtmlContent(html, null);
        builder.toStream(pdf);
        try {
            builder.run();
        } catch (Exception e) {
            throw new PaymentReceiptRenderingException("Failed to render the receipt PDF", e);
        }
        return pdf.toByteArray();
    }

    /** Non-breaking spaces from the French locale would render as boxes in the PDF font, hence the plain space. */
    private static String money(BigDecimal amount) {
        return String.format(FR, "%,.2f MAD", amount).replace(' ', ' ').replace(' ', ' ');
    }

    private static boolean isPositive(BigDecimal amount) {
        return amount != null && amount.signum() > 0;
    }
}
