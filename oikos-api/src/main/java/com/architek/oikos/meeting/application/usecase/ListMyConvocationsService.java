package com.architek.oikos.meeting.application.usecase;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.dto.MyConvocationView;
import com.architek.oikos.meeting.application.port.in.ListMyConvocationsUseCase;
import com.architek.oikos.meeting.application.port.out.OwnedUnitDirectoryPort;
import com.architek.oikos.meeting.application.port.out.OwnedUnitInfo;
import com.architek.oikos.meeting.application.query.ListMyConvocationsQuery;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.GeneralMeetingId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The copropriétaire's own list: one row per lot they own and per meeting that
 * lot is convoked to. An owner of three lots in the same copropriété sees
 * three rows for the same meeting - which is correct, because each lot carries
 * its own voice and its own answer.
 *
 * <p>Drafts are excluded: a meeting still being prepared has not been
 * announced to anyone, and its convocations only exist from CONVENED onwards
 * anyway - the filter guards against a draft that was convoked and walked
 * back.
 */
@Component
public class ListMyConvocationsService implements ListMyConvocationsUseCase {

    private final OwnedUnitDirectoryPort ownedUnitDirectoryPort;
    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;

    public ListMyConvocationsService(OwnedUnitDirectoryPort ownedUnitDirectoryPort,
                                      ConvocationRepository convocationRepository,
                                      GeneralMeetingRepository generalMeetingRepository) {
        this.ownedUnitDirectoryPort = ownedUnitDirectoryPort;
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyConvocationView> listMyConvocations(ListMyConvocationsQuery query) {
        Map<EntityId, OwnedUnitInfo> unitsById = new LinkedHashMap<>();
        for (OwnedUnitInfo unit : ownedUnitDirectoryPort.listOwnedUnits(query.userId())) {
            unitsById.put(unit.unitId(), unit);
        }
        if (unitsById.isEmpty()) {
            return List.of();
        }

        // Meetings are cached across rows: an owner of several lots in one copropriété is
        // convoked to the same meeting once per lot.
        Map<GeneralMeetingId, Optional<GeneralMeeting>> meetingCache = new LinkedHashMap<>();

        return convocationRepository.findByUnitIds(unitsById.keySet()).stream()
                .map(convocation -> toView(convocation, unitsById.get(convocation.getUnitId()),
                        meetingCache.computeIfAbsent(convocation.getGeneralMeetingId(),
                                generalMeetingRepository::findById)))
                .flatMap(Optional::stream)
                .sorted(Comparator.comparing(MyConvocationView::scheduledAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private Optional<MyConvocationView> toView(Convocation convocation, OwnedUnitInfo unit,
                                                Optional<GeneralMeeting> meeting) {
        if (unit == null || meeting.isEmpty() || meeting.get().getStatus() == MeetingStatus.DRAFT) {
            return Optional.empty();
        }
        GeneralMeeting found = meeting.get();
        return Optional.of(new MyConvocationView(convocation.getId(), found.getId(), unit.propertyName(),
                found.getTitle(), found.getMeetingType(), found.getStatus(), found.getScheduledAt(),
                found.getVenue() != null ? found.getVenue().type() : null,
                found.getVenue() != null ? found.getVenue().address() : null,
                found.getVenue() != null ? found.getVenue().link() : null,
                unit.unitNumber(), unit.buildingName(), found.getComment(), convocation.getAttendanceReply(),
                convocation.isCheckedIn()));
    }
}
