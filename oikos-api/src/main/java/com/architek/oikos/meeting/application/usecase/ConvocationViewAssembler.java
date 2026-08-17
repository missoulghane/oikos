package com.architek.oikos.meeting.application.usecase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.dto.ConvocationDeliveryView;
import com.architek.oikos.meeting.application.dto.ConvocationRecipient;
import com.architek.oikos.meeting.application.dto.ConvocationReplyView;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.out.PropertyUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationChannel;
import com.architek.oikos.meeting.domain.model.ConvocationDelivery;
import com.architek.oikos.meeting.domain.model.ConvocationReply;
import com.architek.oikos.meeting.domain.model.ReplyMedium;
import com.architek.oikos.meeting.domain.repository.ReplyMediumRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.ChannelCode;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.ReplyMediumCode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Turns convocations into rows a screen can show: the aggregate holds a lot's
 * id, the table shows "Bâtiment A - Lot 12, M. Benali".
 *
 * <p>The lot registry is fetched once per call and reused for the whole list.
 * Resolving it per row would mean one cross-module walk of the copropriété per
 * lot, which on a hundred-lot property turns one tracking screen into a
 * hundred traversals. The channel and reply-medium catalogs are read the same
 * way - once, then shared - for the same reason.
 */
@Component
public class ConvocationViewAssembler {

    private final PropertyUnitDirectoryPort propertyUnitDirectoryPort;
    private final ConvocationChannelLookup channelLookup;
    private final ReplyMediumRepository replyMediumRepository;
    private final GeneralMeetingRepository generalMeetingRepository;

    public ConvocationViewAssembler(PropertyUnitDirectoryPort propertyUnitDirectoryPort,
                                     ConvocationChannelLookup channelLookup,
                                     ReplyMediumRepository replyMediumRepository,
                                     GeneralMeetingRepository generalMeetingRepository) {
        this.propertyUnitDirectoryPort = propertyUnitDirectoryPort;
        this.channelLookup = channelLookup;
        this.replyMediumRepository = replyMediumRepository;
        this.generalMeetingRepository = generalMeetingRepository;
    }

    /**
     * The whole medium catalog by code, deactivated rows included: an answer
     * recorded before a medium was retired still has to display as something
     * other than a bare code.
     */
    private Map<ReplyMediumCode, ReplyMedium> mediaByCode() {
        Map<ReplyMediumCode, ReplyMedium> byCode = new LinkedHashMap<>();
        for (ReplyMedium medium : replyMediumRepository.findAll()) {
            byCode.put(medium.getCode(), medium);
        }
        return byCode;
    }

    public Map<EntityId, UnitInfo> unitsByIdOf(EntityId propertyId) {
        Map<EntityId, UnitInfo> byId = new LinkedHashMap<>();
        for (UnitInfo unit : propertyUnitDirectoryPort.listUnits(propertyId)) {
            byId.put(unit.unitId(), unit);
        }
        return byId;
    }

    /**
     * List rows, stripped of their confirmation codes. A tracking table shows
     * every lot of the copropriété at once, and a code per row would put every
     * lot's answer within reach of whoever can read that screen - the code is
     * only ever carried by a single lot's detail (ConvocationView.withoutCodes).
     */
    public List<ConvocationView> toViews(List<Convocation> convocations, Map<EntityId, UnitInfo> unitsById) {
        Map<ChannelCode, ConvocationChannel> channelsByCode = channelLookup.byCode();
        Map<ReplyMediumCode, ReplyMedium> mediaByCode = mediaByCode();
        // Cached across rows: a tracking table's hundred convocations belong to one meeting,
        // and resolving its reference per row would be a hundred reads of the same aggregate.
        // The codes are dropped anyway here - this only keeps the shared path honest.
        Map<GeneralMeetingId, String> referenceCache = new HashMap<>();
        return convocations.stream()
                .map(convocation -> toView(convocation, unitsById.get(convocation.getUnitId()), channelsByCode,
                        mediaByCode, publicReferenceOf(convocation.getGeneralMeetingId(), referenceCache))
                        .withoutCodes())
                .toList();
    }

    public ConvocationView toView(Convocation convocation, UnitInfo unit) {
        return toView(convocation, unit, channelLookup.byCode(), mediaByCode(),
                publicReferenceOf(convocation.getGeneralMeetingId(), new HashMap<>()));
    }

    private String publicReferenceOf(GeneralMeetingId meetingId, Map<GeneralMeetingId, String> cache) {
        return cache.computeIfAbsent(meetingId, id -> generalMeetingRepository.findById(id)
                .map(meeting -> meeting.getPublicReference().value()).orElse(null));
    }

    /**
     * A null UnitInfo is tolerated rather than fatal: a lot deleted after its
     * convocation was issued must not make the whole tracking table
     * unreadable - the row shows what it still knows.
     */
    public ConvocationView toView(Convocation convocation, UnitInfo unit,
                                   Map<ChannelCode, ConvocationChannel> channelsByCode,
                                   Map<ReplyMediumCode, ReplyMedium> mediaByCode, String publicReference) {
        List<ConvocationDeliveryView> deliveries = convocation.getDeliveries().stream()
                .map(delivery -> toDeliveryView(delivery, channelsByCode)).toList();
        // Newest first: a history is read from what stands back towards how it got there, and
        // the head of this list is the answer the fields alongside it repeat.
        List<ConvocationReplyView> replies = convocation.getReplies().stream()
                .sorted(ConvocationReply.LATEST_FIRST).map(reply -> toReplyView(reply, mediaByCode)).toList();
        String mediumLabel = labelOf(convocation.getReplyMedium(), mediaByCode);
        if (unit == null) {
            return ConvocationView.from(convocation, null, null, List.of(), deliveries, replies, mediumLabel,
                    publicReference);
        }
        return ConvocationView.from(convocation, unit.unitNumber(), unit.buildingName(),
                unit.owners().stream().map(owner -> new ConvocationRecipient(owner.fullName(), owner.email())).toList(),
                deliveries, replies, mediumLabel, publicReference);
    }

    /**
     * An unknown code falls back to the code itself rather than failing: a
     * channel row deleted outright (rather than deactivated) must not make the
     * deliveries that reference it unreadable.
     */
    private static ConvocationDeliveryView toDeliveryView(ConvocationDelivery delivery,
                                                           Map<ChannelCode, ConvocationChannel> channelsByCode) {
        ConvocationChannel channel = channelsByCode.get(delivery.getChannelCode());
        String code = delivery.getChannelCode().value();
        return new ConvocationDeliveryView(delivery.getId().toString(), code,
                channel != null ? channel.getLabel() : code, delivery.getStatus(), delivery.getSentAt(),
                delivery.getReference(), delivery.isReminder());
    }

    /** Same fallback as a delivery's channel: an unknown code displays as itself. */
    private static String labelOf(ReplyMediumCode code, Map<ReplyMediumCode, ReplyMedium> mediaByCode) {
        if (code == null) {
            return null;
        }
        ReplyMedium medium = mediaByCode.get(code);
        return medium != null ? medium.getLabel() : code.value();
    }

    private static ConvocationReplyView toReplyView(ConvocationReply reply,
                                                     Map<ReplyMediumCode, ReplyMedium> mediaByCode) {
        return new ConvocationReplyView(reply.getId().toString(), reply.getReply(), reply.getAttendanceMode(),
                reply.isByProxy(), reply.getSource(),
                reply.getMedium() != null ? reply.getMedium().value() : null, labelOf(reply.getMedium(), mediaByCode),
                reply.getNote(), reply.getReceivedAt(), reply.getCreatedDate());
    }
}
