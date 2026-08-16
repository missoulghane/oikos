package com.architek.oikos.meeting.application.usecase;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.AgendaItemResultView;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.valueobject.AttendanceTally;
import com.architek.oikos.meeting.domain.valueobject.MeetingType;
import com.architek.oikos.meeting.domain.valueobject.MeetingVenue;
import com.architek.oikos.meeting.domain.valueobject.VoteOutcome;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Turns a closed session into the text of its procès-verbal.
 *
 * <p>This is where everything the module computes stops being computed. The
 * attendance, the tallies and the outcomes are rendered into HTML once, and
 * that HTML is what the minutes then are - a lot sold or a tantième corrected
 * next month cannot restate a meeting already minuted (see MeetingMinutes).
 *
 * <p>The text is French and deliberately factual: who was present and for how
 * many voices, then point by point the rule applied, the figures and the
 * outcome. Anything a syndic wants to add - debates, remarks, an opposition a
 * copropriétaire asked to have recorded - is added by editing the draft. The
 * generator does not attempt prose it cannot know.
 */
@Component
public class MinutesComposer {

    private static final Locale FR = Locale.FRANCE;
    private static final ZoneId CASABLANCA = ZoneId.of("Africa/Casablanca");
    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH'h'mm", FR).withZone(CASABLANCA);

    public String compose(GeneralMeeting meeting, String propertyName, List<Convocation> convocations,
                           Map<EntityId, UnitInfo> unitsById, List<AgendaItemResultView> results) {
        AttendanceTally attendance = AttendanceTally.of(convocations);
        StringBuilder html = new StringBuilder();

        html.append("<h1>Procès-verbal — ").append(escape(meeting.getTitle())).append("</h1>");
        html.append("<p>Assemblée générale ")
                .append(meeting.getMeetingType() == MeetingType.EXTRAORDINARY ? "extraordinaire" : "ordinaire")
                .append(" de la copropriété <strong>").append(escape(propertyName)).append("</strong>,")
                .append(" tenue le ").append(meeting.getScheduledAt() == null ? "—"
                        : DATE_TIME.format(meeting.getScheduledAt()))
                .append(", ").append(escape(venueSentence(meeting.getVenue()))).append(".</p>");

        appendAttendance(html, meeting, attendance, convocations, unitsById);
        appendResolutions(html, results);

        html.append("<h2>Observations</h2>");
        html.append("<p><em>À compléter par le syndic avant validation.</em></p>");
        return html.toString();
    }

    private void appendAttendance(StringBuilder html, GeneralMeeting meeting, AttendanceTally attendance,
                                   List<Convocation> convocations, Map<EntityId, UnitInfo> unitsById) {
        html.append("<h2>Présences</h2>");
        html.append("<p>").append(attendance.checkedInCount()).append(" lot(s) présent(s) ou représenté(s) sur ")
                .append(attendance.totalUnits()).append(", totalisant ")
                .append(plain(attendance.presentWeight().value())).append(" voix sur ")
                .append(plain(attendance.totalWeight().value())).append(".</p>");

        if (meeting.getQuorumPercentage().isRequired()) {
            boolean reached = attendance.isQuorumReachedFor(meeting.getQuorumPercentage());
            html.append("<p>Quorum requis : ").append(plain(meeting.getQuorumPercentage().value()))
                    .append(" % — ").append(reached ? "atteint" : "non atteint").append(".</p>");
        } else {
            html.append("<p>Aucun quorum n'était requis pour cette assemblée.</p>");
        }
        // Printed in full, never as a footnote: opening without quorum is a decision with legal
        // consequences and the record has to carry it (ADR 0002 §5).
        if (meeting.isOpenedWithoutQuorum()) {
            html.append("<p><strong>La séance a été ouverte alors que le quorum n'était pas atteint.</strong></p>");
        }

        html.append("<h3>Lots présents ou représentés</h3>");
        List<String> present = convocations.stream().filter(Convocation::isCheckedIn)
                .map(convocation -> lotLabel(unitsById.get(convocation.getUnitId()), convocation)).sorted().toList();
        if (present.isEmpty()) {
            html.append("<p><em>Aucun lot présent.</em></p>");
        } else {
            html.append("<ul>");
            present.forEach(label -> html.append("<li>").append(escape(label)).append("</li>"));
            html.append("</ul>");
        }
    }

    private void appendResolutions(StringBuilder html, List<AgendaItemResultView> results) {
        html.append("<h2>Résolutions</h2>");
        if (results.isEmpty()) {
            html.append("<p><em>Aucun point à l'ordre du jour.</em></p>");
            return;
        }
        int number = 1;
        for (AgendaItemResultView result : results) {
            html.append("<h3>Résolution n° ").append(number++).append(" — ").append(escape(result.label()))
                    .append("</h3>");
            html.append("<p>Majorité applicable : ").append(majorityLabel(result)).append("</p>");
            html.append("<p>Pour : ").append(result.forCount()).append(" lot(s), ")
                    .append(plain(result.forWeight())).append(" voix — Contre : ").append(result.againstCount())
                    .append(" lot(s), ").append(plain(result.againstWeight()))
                    .append(" voix — Abstentions : ").append(result.abstentionCount()).append(" lot(s), ")
                    .append(plain(result.abstentionWeight())).append(" voix.</p>");
            html.append("<p>Voix exprimées : ").append(plain(result.expressedWeight()))
                    .append(" — voix présentes : ").append(plain(result.presentWeight()))
                    .append(" — voix de la copropriété : ").append(plain(result.totalWeight())).append(".</p>");
            html.append("<p><strong>Résolution ")
                    .append(result.outcome() == VoteOutcome.ADOPTED ? "adoptée" : "rejetée")
                    .append(".</strong></p>");
        }
    }

    private static String majorityLabel(AgendaItemResultView result) {
        return switch (result.majorityRule()) {
            case SIMPLE -> "majorité simple des voix exprimées";
            case ABSOLUTE -> "majorité absolue des voix de la copropriété";
            case UNANIMITY -> "unanimité des lots présents";
        };
    }

    private static String venueSentence(MeetingVenue venue) {
        if (venue == null) {
            return "en un lieu non précisé";
        }
        return switch (venue.type()) {
            case PHYSICAL -> "à " + venue.address();
            case VIDEOCONFERENCE -> "en visioconférence";
            case HYBRID -> "à " + venue.address() + " et en visioconférence";
        };
    }

    private static String lotLabel(UnitInfo unit, Convocation convocation) {
        String label = unit == null ? convocation.getUnitId().toString()
                : (unit.buildingName() == null ? unit.unitNumber() : unit.buildingName() + " - " + unit.unitNumber());
        return label + " (" + plain(convocation.getVotingWeight().value()) + " voix)";
    }

    private static String plain(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    /**
     * Property names, lot numbers and addresses are free text typed by a
     * syndic; interpolated raw into the minutes, a stray angle bracket would
     * corrupt the document that matters most.
     */
    private static String escape(String value) {
        if (value == null) {
            return "—";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
