package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.ConvocationDocumentView;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.AgendaItem;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.AgendaItemRepository;

/**
 * Builds what the convocation letter prints: the assembly and its agenda, the
 * lot, and the owners it is addressed to.
 *
 * <p>Shared by the sending path and the download path so that the PDF a syndic
 * prints to post is byte-for-byte the one an owner receives by email. Two
 * builders would eventually disagree, and the disagreement would only surface
 * in a contested assembly.
 */
@Component
public class ConvocationDocumentComposer {

    private final AgendaItemRepository agendaItemRepository;
    private final ConvocationLinkComposer linkComposer;

    public ConvocationDocumentComposer(AgendaItemRepository agendaItemRepository,
                                        ConvocationLinkComposer linkComposer) {
        this.agendaItemRepository = agendaItemRepository;
        this.linkComposer = linkComposer;
    }

    public ConvocationDocumentView compose(Convocation convocation, GeneralMeeting meeting, UnitInfo unit,
                                            String propertyName) {
        List<String> agendaLabels = agendaItemRepository.findByGeneralMeetingId(meeting.getId()).stream()
                .map(AgendaItem::getLabel).toList();
        return new ConvocationDocumentView(propertyName, meeting.getTitle(), meeting.getMeetingType().name(),
                meeting.getScheduledAt(),
                meeting.getVenue() != null ? meeting.getVenue().type() : null,
                meeting.getVenue() != null ? meeting.getVenue().address() : null,
                meeting.getVenue() != null ? meeting.getVenue().link() : null,
                unit != null ? unit.unitNumber() : null,
                unit != null ? unit.buildingName() : null,
                unit == null ? List.of() : unit.owners().stream().map(OwnerInfo::fullName).toList(),
                agendaLabels, meeting.getQuorumPercentage().value().toPlainString(),
                linkComposer.link(convocation.getConfirmationToken()),
                RichTextToParagraphs.convert(meeting.getComment()));
    }

    /** Keeps the attachment name readable and free of anything a file system would object to. */
    public static String fileNameFor(UnitInfo unit) {
        String raw = unit == null || unit.unitNumber() == null ? "lot" : unit.unitNumber();
        String slug = raw.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return "convocation-" + (slug.isEmpty() ? "lot" : slug) + ".pdf";
    }
}
