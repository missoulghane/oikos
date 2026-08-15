package com.architek.oikos.installment.infrastructure.receipt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.architek.oikos.installment.application.dto.PaymentReceiptView;
import com.architek.oikos.installment.domain.valueobject.PaymentMode;

/**
 * Renders through the real Thymeleaf + openhtmltopdf pipeline and reads the
 * produced PDF back with PDFBox, so the assertions are about what actually lands
 * in the document rather than about the HTML on the way there.
 */
class ThymeleafPaymentReceiptRendererTest {

    private final ThymeleafPaymentReceiptRenderer renderer = new ThymeleafPaymentReceiptRenderer(templateEngine());

    /**
     * SpringTemplateEngine, not the bare TemplateEngine: the Spring dialect
     * evaluates expressions with SpEL, while the standard one needs OGNL, which
     * Boot's Thymeleaf starter does not put on the classpath. Using the bare
     * engine here would test a pipeline production never runs.
     */
    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private static PaymentReceiptView receipt(String propertyName, List<String> payers) {
        return new PaymentReceiptView("PAY-2026-001", propertyName, "A12", payers, LocalDate.of(2026, 3, 15),
                PaymentMode.CHECK, new BigDecimal("2500.00"), new BigDecimal("2000.00"), new BigDecimal("500.00"), 2);
    }

    private static String textOf(byte[] pdf) throws Exception {
        try (PDDocument document = Loader.loadPDF(new ByteArrayInputStream(pdf).readAllBytes())) {
            return new PDFTextStripper().getText(document);
        }
    }

    @Test
    void produces_a_real_pdf() {
        byte[] pdf = renderer.render(receipt("Résidence Al Amal", List.of("Rachid Tazi")));

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1))
                .as("a PDF must start with the %PDF- magic bytes")
                .isEqualTo("%PDF-");
    }

    @Test
    void lays_out_the_payment_the_receipt_attests_to() throws Exception {
        String text = textOf(renderer.render(receipt("Résidence Al Amal", List.of("Rachid Tazi"))));

        assertThat(text).contains("PAY-2026-001");
        assertThat(text).contains("Résidence Al Amal");
        assertThat(text).contains("Lot A12");
        assertThat(text).contains("Rachid Tazi");
        assertThat(text).contains("15/03/2026");
        assertThat(text).contains("Chèque");
    }

    @Test
    void formats_amounts_in_french_without_unprintable_separators() throws Exception {
        String text = textOf(renderer.render(receipt("Résidence Al Amal", List.of("Rachid Tazi"))));

        // 2500.00 -> "2 500,00 MAD", with a plain space: the locale's narrow
        // no-break space is absent from the base font and would render blank.
        assertThat(text).contains("2 500,00 MAD");
        assertThat(text).doesNotContain(" ").doesNotContain(" ");
    }

    @Test
    void breaks_the_amount_down_between_settled_echeances_and_advance() throws Exception {
        String text = textOf(renderer.render(receipt("Résidence Al Amal", List.of("Rachid Tazi"))));

        assertThat(text).contains("2 échéance(s)");
        assertThat(text).contains("2 000,00 MAD");
        assertThat(text).contains("Conservé en avance");
        assertThat(text).contains("500,00 MAD");
    }

    @Test
    void omits_the_breakdown_when_nothing_was_settled_nor_kept() throws Exception {
        PaymentReceiptView noBreakdown = new PaymentReceiptView("PAY-2", "Nour", "B3", List.of("Salma Idrissi"),
                LocalDate.of(2026, 1, 5), PaymentMode.CASH, new BigDecimal("100.00"), BigDecimal.ZERO,
                BigDecimal.ZERO, 0);

        String text = textOf(renderer.render(noBreakdown));

        assertThat(text).doesNotContain("Affectation").doesNotContain("Conservé en avance");
    }

    // A lot with no owner attached is legal (OwnershipStatus.NOT_AFFECTED) and
    // must still produce a receipt rather than blow up on a null.
    @Test
    void renders_a_lot_that_has_no_owner_attached() throws Exception {
        String text = textOf(renderer.render(receipt("Résidence Al Amal", List.of())));

        assertThat(text).contains("Lot A12").doesNotContain("Copropriétaire");
    }

    // Thymeleaf escapes by default; a hand-rolled placeholder substitution would
    // have produced malformed XHTML here and failed the render.
    @Test
    void escapes_a_name_carrying_markup_characters() throws Exception {
        String text = textOf(renderer.render(receipt("Résidence <Al & Amal>", List.of("Tazi & Fils"))));

        assertThat(text).contains("Résidence <Al & Amal>");
        assertThat(text).contains("Tazi & Fils");
    }

    @Test
    void surfaces_a_rendering_failure_rather_than_a_corrupt_document() {
        ThymeleafPaymentReceiptRenderer broken = new ThymeleafPaymentReceiptRenderer(new SpringTemplateEngine());

        assertThatThrownBy(() -> broken.render(receipt("Résidence", List.of("X"))))
                .isInstanceOf(RuntimeException.class);
    }
}
