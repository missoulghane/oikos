package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.PublishMeetingMinutesCommand;
import com.architek.oikos.meeting.application.dto.MeetingMinutesView;
import com.architek.oikos.meeting.application.port.in.PublishMeetingMinutesUseCase;
import com.architek.oikos.meeting.application.port.out.MeetingMinutesDocumentPort;
import com.architek.oikos.meeting.application.port.out.MeetingMinutesRendererPort;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.MeetingMinutesNotFoundException;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.model.MeetingMinutes;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.repository.MeetingMinutesRepository;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Publishes the minutes: renders the validated text to PDF, files it, moves
 * the meeting to MINUTES_PUBLISHED and tells the copropriétaires.
 *
 * <p>The PDF is rendered from the stored content, never from a fresh
 * computation of the figures. What is diffused has to be the document that was
 * validated - recomputing at print time would let a correction made in between
 * appear in a record nobody approved.
 *
 * <p>Everything happens in one transaction, unlike the convocation dispatch.
 * Publishing is a single deliberate act on a single document, and it is
 * separately permissioned (meeting:minutes:publish): if the PDF cannot be
 * produced, the right outcome is that nothing was published at all - not
 * minutes marked as diffused with no document behind them.
 */
@Component
public class PublishMeetingMinutesService implements PublishMeetingMinutesUseCase {

    private static final Logger log = LoggerFactory.getLogger(PublishMeetingMinutesService.class);

    private final MeetingMinutesRepository meetingMinutesRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final MeetingMinutesRendererPort rendererPort;
    private final MeetingMinutesDocumentPort documentPort;
    private final ConvocationViewAssembler convocationViewAssembler;
    private final MeetingNotificationDispatcher notificationDispatcher;
    private final Clock clock;

    public PublishMeetingMinutesService(MeetingMinutesRepository meetingMinutesRepository,
                                         GeneralMeetingRepository generalMeetingRepository,
                                         PropertyDirectoryPort propertyDirectoryPort,
                                         MeetingMinutesRendererPort rendererPort,
                                         MeetingMinutesDocumentPort documentPort,
                                         ConvocationViewAssembler convocationViewAssembler,
                                         MeetingNotificationDispatcher notificationDispatcher, Clock clock) {
        this.meetingMinutesRepository = meetingMinutesRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.rendererPort = rendererPort;
        this.documentPort = documentPort;
        this.convocationViewAssembler = convocationViewAssembler;
        this.notificationDispatcher = notificationDispatcher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MeetingMinutesView publish(PublishMeetingMinutesCommand command) {
        MeetingMinutes minutes = meetingMinutesRepository.findByGeneralMeetingId(command.generalMeetingId())
                .orElseThrow(() -> new MeetingMinutesNotFoundException(command.generalMeetingId()));
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        MeetingMinutes published = minutes.publish(clock.instant());
        String propertyName = propertyDirectoryPort.getProperty(meeting.getPropertyId()).name();

        byte[] pdf = rendererPort.render(propertyName, meeting.getTitle(), published.getContent());
        documentPort.replaceMinutesDocument(meeting.getId(), "proces-verbal-" + meeting.getId() + ".pdf", pdf,
                command.publishedByUserId());

        generalMeetingRepository.save(meeting.markMinutesPublished());
        MeetingMinutes saved = meetingMinutesRepository.save(published);

        notifyOwners(meeting, propertyName);
        log.info("Minutes published for general meeting {}", meeting.getId());
        return MeetingMinutesView.from(saved);
    }

    /**
     * Best effort, and deliberately so: the minutes are published because the
     * PDF exists and the record says so, not because a notification landed. A
     * notification service being down must not leave a meeting stuck between
     * two states.
     *
     * <p>Which is exactly what it did until the dispatch moved to
     * MeetingNotificationDispatcher. Everything here runs in one transaction (see
     * this class's header), the notification's own services joined it, and a
     * failure among them marked it rollback-only before this catch ever saw the
     * exception - publishing the minutes then failed at commit, over a
     * notification. REQUIRES_NEW on the other side is what keeps the two apart.
     */
    private void notifyOwners(GeneralMeeting meeting, String propertyName) {
        try {
            Map<EntityId, UnitInfo> unitsById = convocationViewAssembler.unitsByIdOf(meeting.getPropertyId());
            List<EntityId> partyIds = unitsById.values().stream().flatMap(unit -> unit.owners().stream())
                    .map(OwnerInfo::partyId).distinct().toList();
            notificationDispatcher.notifyMinutesPublished(partyIds, meeting);
        } catch (RuntimeException e) {
            log.warn("Notification of published minutes failed for general meeting {} on {} - the publication stands",
                    meeting.getId(), propertyName, e);
        }
    }
}
