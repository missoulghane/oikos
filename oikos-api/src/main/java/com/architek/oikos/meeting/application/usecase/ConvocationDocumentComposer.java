package com.architek.oikos.meeting.application.usecase;

import java.util.List;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.ConvocationDocumentView;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.QrCodeRendererPort;
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
    private final QrCodeRendererPort qrCodeRendererPort;

    public ConvocationDocumentComposer(AgendaItemRepository agendaItemRepository,
                                        ConvocationLinkComposer linkComposer,
                                        QrCodeRendererPort qrCodeRendererPort) {
        this.agendaItemRepository = agendaItemRepository;
        this.linkComposer = linkComposer;
        this.qrCodeRendererPort = qrCodeRendererPort;
    }

    public ConvocationDocumentView compose(Convocation convocation, GeneralMeeting meeting, UnitInfo unit,
                                            String propertyName) {
        List<String> agendaLabels = agendaItemRepository.findByGeneralMeetingId(meeting.getId()).stream()
                .map(AgendaItem::getLabel).toList();
        String confirmationLink = linkComposer.link(convocation.getConfirmationToken());
        return new ConvocationDocumentView(propertyName, meeting.getTitle(), meeting.getMeetingType().name(),
                meeting.getScheduledAt(),
                meeting.getVenue() != null ? meeting.getVenue().type() : null,
                meeting.getVenue() != null ? meeting.getVenue().address() : null,
                meeting.getVenue() != null ? meeting.getVenue().link() : null,
                unit != null ? unit.unitNumber() : null,
                unit != null ? unit.buildingName() : null,
                unit == null ? List.of() : unit.owners().stream().map(OwnerInfo::fullName).toList(),
                agendaLabels, meeting.getQuorumPercentage().value().toPlainString(), confirmationLink,
                // The QR encodes the TOKEN link, never the short code: a scan has no reason to
                // fall back on the weaker secret, and the strong one costs nothing to encode.
                qrCodeRendererPort.renderAsDataUri(confirmationLink),
                meeting.getPublicReference().value(), convocation.getConfirmationCode().value(),
                RichTextToParagraphs.convert(meeting.getComment()));
    }

    /**
     * The lot as a person names it - "Bâtiment A - Appartement 12". Used as the
     * "concerne" hint on a messagerie thread, which is what tells an owner of
     * three lots which of them a convocation is about.
     */
    public static String lotLabelOf(UnitInfo unit) {
        if (unit == null || unit.unitNumber() == null) {
            return null;
        }
        return unit.buildingName() == null ? unit.unitNumber() : unit.buildingName() + " - " + unit.unitNumber();
    }

    /** Keeps the attachment name readable and free of anything a file system would object to. */
    public static String fileNameFor(UnitInfo unit) {
        String raw = unit == null || unit.unitNumber() == null ? "lot" : unit.unitNumber();
        String slug = raw.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return "convocation-" + (slug.isEmpty() ? "lot" : slug) + ".pdf";
    }
}
