package com.architek.oikos.meeting.infrastructure.convocation;

import java.io.ByteArrayOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

import com.architek.oikos.meeting.application.dto.ConvocationDocumentView;
import com.architek.oikos.meeting.application.port.out.ConvocationRendererPort;

/**
 * Renders the convocation to HTML (Thymeleaf), then that HTML to a PDF
 * (openhtmltopdf / PDFBox) - the exact chain ThymeleafPaymentReceiptRenderer
 * already uses, kept behind ConvocationRendererPort so no other layer imports
 * either library.
 *
 * <p>All French formatting happens here rather than in the view: how a date or
 * a venue reads is presentation. The timezone is Africa/Casablanca - an
 * instant rendered in UTC would print the wrong hour on the one document whose
 * hour matters.
 */
@Component
@RequiredArgsConstructor
public class ThymeleafConvocationRenderer implements ConvocationRendererPort {

    private static final String TEMPLATE = "convocation/convocation";
    private static final Locale FR = Locale.FRANCE;
    private static final ZoneId CASABLANCA = ZoneId.of("Africa/Casablanca");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", FR).withZone(CASABLANCA);

    private final ITemplateEngine templateEngine;

    @Override
    public byte[] render(ConvocationDocumentView convocation) {
        Context context = new Context(FR);
        context.setVariable("propertyName", convocation.propertyName());
        context.setVariable("meetingTitle", convocation.meetingTitle());
        context.setVariable("meetingType", "EXTRAORDINARY".equals(convocation.meetingType())
                ? "Assemblée générale extraordinaire" : "Assemblée générale ordinaire");
        context.setVariable("scheduledAt", convocation.scheduledAt() == null ? "à préciser"
                : DATE_TIME.format(convocation.scheduledAt()));
        context.setVariable("venue", venueLabel(convocation));
        context.setVariable("unitLabel", unitLabel(convocation));
        context.setVariable("ownerNames", convocation.ownerNames().isEmpty() ? null
                : String.join(", ", convocation.ownerNames()));
        context.setVariable("agendaLabels", convocation.agendaLabels());
        context.setVariable("quorumPercentage", convocation.quorumPercentage());
        context.setVariable("confirmationLink", convocation.confirmationLink());
        context.setVariable("commentParagraphs", convocation.commentParagraphs());

        String html = templateEngine.process(TEMPLATE, context);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        // No base URI: the template embeds its own CSS and pulls no external asset.
        builder.withHtmlContent(html, null);
        builder.toStream(pdf);
        try {
            builder.run();
        } catch (Exception e) {
            throw new ConvocationRenderingException("Failed to render the convocation PDF", e);
        }
        return pdf.toByteArray();
    }

    private static String unitLabel(ConvocationDocumentView convocation) {
        if (convocation.unitNumber() == null) {
            return "—";
        }
        return convocation.buildingName() == null ? convocation.unitNumber()
                : convocation.buildingName() + " - " + convocation.unitNumber();
    }

    private static String venueLabel(ConvocationDocumentView convocation) {
        if (convocation.venueType() == null) {
            return "à préciser";
        }
        return switch (convocation.venueType()) {
            case PHYSICAL -> convocation.venueAddress();
            case VIDEOCONFERENCE -> "Visioconférence : " + convocation.venueLink();
            case HYBRID -> convocation.venueAddress() + " (et en visioconférence : " + convocation.venueLink() + ")";
        };
    }
}
