package com.architek.oikos.meeting.application.usecase;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.shared.application.port.out.EmailTemplateRendererPort;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;

/**
 * Composes the subject and body of the convocation email, in French - the
 * language the copropriétaires read, as everywhere else in the product's
 * user-facing text. Package-private: only SendConvocationService uses it,
 * same patron as PasswordResetEmailComposer.
 *
 * <p>The email is a covering note, not the convocation itself: the letter is
 * the attached PDF, and it is the document that has legal standing. Hence the
 * deliberate brevity here, and the reminder variant differing only by its
 * opening line.
 *
 * <p>The messagerie shares that wording through {@link #inAppBody}, with one
 * difference that is not cosmetic: it cannot say the convocation is attached,
 * because the messagerie carries no attachment. It points at the
 * copropriétaire's own space instead, where the PDF is already downloadable -
 * GET /convocations/&#123;id&#125;/document is open to the lot's owner, not only to
 * the syndic.
 *
 * <p>The emails are rendered from templates/email/convocation.html, inside the
 * common layout. The in-app body stays here: the messagerie shows a fragment,
 * not a whole email page.
 */
@Component
class ConvocationEmailComposer {

    private static final Locale FR = Locale.FRANCE;
    private static final ZoneId CASABLANCA = ZoneId.of("Africa/Casablanca");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", FR).withZone(CASABLANCA);

    private final EmailTemplateRendererPort templateRenderer;

    ConvocationEmailComposer(EmailTemplateRendererPort templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    String subject(String propertyName, GeneralMeeting meeting) {
        return "Daba Syndic - Convocation : " + meeting.getTitle() + " (" + propertyName + ")";
    }

    String reminderSubject(String propertyName, GeneralMeeting meeting) {
        return "Daba Syndic - Rappel : " + meeting.getTitle() + " (" + propertyName + ")";
    }

    String htmlBody(String propertyName, GeneralMeeting meeting, UnitInfo unit, String confirmationLink) {
        return emailBody(propertyName, meeting, unit, confirmationLink, false);
    }

    /**
     * The same note, for a reader who is already signed in. No confirmation
     * link: the recipient has an account, so they answer from their space,
     * which records who answered rather than only that the link was held
     * (ADR 0002 §10). And no "attached to this email", which would be false.
     */
    String inAppBody(String propertyName, GeneralMeeting meeting, UnitInfo unit) {
        return """
                <p>Vous êtes convoqué(e) à l'assemblée générale suivante :</p>
                <p><strong>%s</strong> — assemblée générale %s<br/>
                Copropriété : %s<br/>
                Lot concerné : %s<br/>
                Date : %s<br/>
                Lieu : %s</p>
                <p>La convocation détaillée, avec l'ordre du jour, est disponible dans votre espace
                    copropriétaire, où vous pouvez également indiquer votre présence ou votre absence.</p>
                """.formatted(escape(meeting.getTitle()), typeLabel(meeting.getMeetingType()), escape(propertyName),
                escape(lotLabel(unit)), dateLabel(meeting.getScheduledAt()), escape(venueLabel(meeting.getVenue())));
    }

    String reminderHtmlBody(String propertyName, GeneralMeeting meeting, UnitInfo unit, String confirmationLink) {
        return emailBody(propertyName, meeting, unit, confirmationLink, true);
    }

    /**
     * The template escapes every value itself - property names, lot numbers and
     * addresses are free text entered by a syndic.
     *
     * <p>The confirmation link comes first, before the fallback to the
     * copropriétaire's space: a large share of recipients have no account and
     * cannot get one. The page asks for the lot's code, which the email never
     * prints - link and code together would answer the question the code is
     * there to ask (ADR 0002 §16).
     */
    private String emailBody(String propertyName, GeneralMeeting meeting, UnitInfo unit, String confirmationLink,
                             boolean reminder) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("reminder", reminder);
        variables.put("meetingTitle", meeting.getTitle());
        variables.put("meetingType", typeLabel(meeting.getMeetingType()));
        variables.put("propertyName", propertyName);
        variables.put("lotLabel", lotLabel(unit));
        variables.put("dateLabel", dateLabel(meeting.getScheduledAt()));
        variables.put("venueLabel", venueLabel(meeting.getVenue()));
        // Map.of refuses nulls, and "no link" is a legitimate value here.
        variables.put("confirmationLink", confirmationLink == null || confirmationLink.isBlank() ? null : confirmationLink);
        return templateRenderer.render("convocation", variables);
    }

    private static String typeLabel(MeetingType type) {
        return type == MeetingType.EXTRAORDINARY ? "extraordinaire" : "ordinaire";
    }

    private static String lotLabel(UnitInfo unit) {
        if (unit == null) {
            return "—";
        }
        return unit.buildingName() == null ? unit.unitNumber() : unit.buildingName() + " - " + unit.unitNumber();
    }

    /** A meeting past DRAFT always has a date; the guard is for the draft that never should reach here. */
    private static String dateLabel(Instant scheduledAt) {
        return scheduledAt == null ? "à préciser" : DATE_TIME.format(scheduledAt);
    }

    private static String venueLabel(MeetingVenue venue) {
        if (venue == null) {
            return "à préciser";
        }
        return switch (venue.type()) {
            case PHYSICAL -> venue.address();
            case VIDEOCONFERENCE -> "Visioconférence : " + venue.link();
            case HYBRID -> venue.address() + " (et en visioconférence : " + venue.link() + ")";
        };
    }

    /**
     * For the in-app body only. Property names, lot numbers and addresses are
     * free text entered by a syndic; interpolated raw into HTML, a stray &lt; would break the email
     * silently - and an injected tag would not be silent at all.
     */
    private static String escape(String value) {
        if (value == null) {
            return "—";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
