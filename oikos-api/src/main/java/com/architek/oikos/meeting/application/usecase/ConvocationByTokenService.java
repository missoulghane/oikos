package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.domain.exception.ConfirmationClosedException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.InvalidConvocationTokenException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;

/**
 * The confirmation link received with the convocation: what it shows, and what
 * it records.
 *
 * <p>It exists because a large share of lots have no account behind them. Their
 * owners receive the convocation and, until now, had no way of answering other
 * than telephoning the syndic - which is why every reply in the system was
 * SYNDIC_OFFICE. The link is what makes {@link ReplySource#OWNER_LINK} a real
 * answer rather than a hypothetical one.
 *
 * <p>Both entry points are anonymous, which shapes the whole class:
 *
 * <ul>
 * <li>The token is the only credential, so it is the only thing looked up. An
 * unknown one yields the same flat "not valid" for every wrong token - saying
 * more would turn the endpoint into an oracle.</li>
 * <li>The view is deliberately narrow (see ConvocationConfirmationView): a
 * leaked link must expose one lot's convocation and nothing of the
 * copropriété.</li>
 * <li>The source is not read from the request - it cannot be. Reaching this
 * class <em>is</em> the proof that the answer came through the link.</li>
 * <li>repliedByPartyId stays null: the token proves the convocation was
 * received, never which of several indivisaires is clicking.</li>
 * </ul>
 *
 * <p>The link has no expiry date of its own. It stops accepting an answer when
 * the session opens, because from that moment presence is the check-in and not
 * a declaration (ADR 0002 §5). A fixed TTL would say less - it would ignore a
 * postponed meeting - and would drift the day the date moves.
 */
@Component
public class ConvocationByTokenService implements GetConvocationByTokenUseCase, ConfirmConvocationByTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConvocationByTokenService.class);

    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler viewAssembler;
    private final Clock clock;

    public ConvocationByTokenService(ConvocationRepository convocationRepository,
                                      GeneralMeetingRepository generalMeetingRepository,
                                      PropertyDirectoryPort propertyDirectoryPort,
                                      ConvocationViewAssembler viewAssembler, Clock clock) {
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocationConfirmationView getByToken(GetConvocationByTokenQuery query) {
        Convocation convocation = require(query.token());
        return toView(convocation, requireMeetingOf(convocation));
    }

    @Override
    @Transactional
    public ConvocationConfirmationView confirm(ConfirmConvocationByTokenCommand command) {
        Convocation convocation = require(command.token());
        GeneralMeeting meeting = requireMeetingOf(convocation);
        if (!isOpenForConfirmation(meeting)) {
            throw new ConfirmationClosedException();
        }

        AttendanceReply reply = command.attendanceReply();
        Convocation answered = convocation.reply(reply,
                reply == AttendanceReply.NO_REPLY ? null : ReplySource.OWNER_LINK, null, null, clock.instant());
        // The lot, not the person: nothing here identifies who followed the link, and the log
        // must not pretend otherwise.
        log.info("Convocation {} answered {} through its confirmation link", convocation.getId(), reply);
        return toView(convocationRepository.save(answered), meeting);
    }

    private Convocation require(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidConvocationTokenException();
        }
        return convocationRepository.findByConfirmationToken(token)
                .orElseThrow(InvalidConvocationTokenException::new);
    }

    private GeneralMeeting requireMeetingOf(Convocation convocation) {
        return generalMeetingRepository.findById(convocation.getGeneralMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(convocation.getGeneralMeetingId()));
    }

    /** Everything up to the opening of the session; nothing from it on. */
    private static boolean isOpenForConfirmation(GeneralMeeting meeting) {
        MeetingStatus status = meeting.getStatus();
        return status == MeetingStatus.DRAFT || status == MeetingStatus.SCHEDULED || status == MeetingStatus.CONVENED;
    }

    /**
     * The lot is resolved through the same directory the tracking table uses.
     * Naming it matters more here than anywhere else: the visitor has to
     * recognise their own lot before confirming, and "Appartement 12" is what
     * their letter says.
     */
    private ConvocationConfirmationView toView(Convocation convocation, GeneralMeeting meeting) {
        UnitInfo unit = viewAssembler.unitsByIdOf(meeting.getPropertyId()).get(convocation.getUnitId());
        String propertyName = propertyDirectoryPort.getProperty(meeting.getPropertyId()).name();
        return new ConvocationConfirmationView(propertyName, meeting.getTitle(), meeting.getMeetingType().name(),
                meeting.getScheduledAt(), meeting.getVenue() != null ? meeting.getVenue().type() : null,
                meeting.getVenue() != null ? meeting.getVenue().address() : null,
                meeting.getVenue() != null ? meeting.getVenue().link() : null,
                unit != null ? unit.unitNumber() : null, unit != null ? unit.buildingName() : null,
                convocation.getAttendanceReply(), convocation.getRepliedAt(), isOpenForConfirmation(meeting));
    }
}
