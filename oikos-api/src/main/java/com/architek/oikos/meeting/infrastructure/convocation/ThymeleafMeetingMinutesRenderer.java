package com.architek.oikos.meeting.infrastructure.convocation;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import lombok.RequiredArgsConstructor;

import com.architek.oikos.meeting.application.port.out.MeetingMinutesRendererPort;

/**
 * Renders the validated minutes to PDF. The body is the stored HTML, injected
 * with th:utext rather than th:text: it is markup the syndic wrote in a
 * rich-text editor, and escaping it would print the tags instead of the
 * document.
 *
 * <p>Nothing is recomputed here. The renderer receives the frozen content and
 * prints it - what is diffused has to be the document that was validated.
 */
@Component
@RequiredArgsConstructor
public class ThymeleafMeetingMinutesRenderer implements MeetingMinutesRendererPort {

    private static final String TEMPLATE = "minutes/meeting-minutes";
    private static final Locale FR = Locale.FRANCE;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy", FR);
    private static final ZoneId CASABLANCA = ZoneId.of("Africa/Casablanca");

    private final ITemplateEngine templateEngine;

    @Override
    public byte[] render(String propertyName, String meetingTitle, String htmlContent) {
        Context context = new Context(FR);
        context.setVariable("propertyName", propertyName);
        context.setVariable("meetingTitle", meetingTitle);
        context.setVariable("issuedOn", DATE.format(LocalDate.now(CASABLANCA)));
        context.setVariable("content", htmlContent);

        String html = templateEngine.process(TEMPLATE, context);

        ByteArrayOutputStream pdf = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(html, null);
        builder.toStream(pdf);
        try {
            builder.run();
        } catch (Exception e) {
            throw new ConvocationRenderingException("Failed to render the minutes PDF", e);
        }
        return pdf.toByteArray();
    }
}
