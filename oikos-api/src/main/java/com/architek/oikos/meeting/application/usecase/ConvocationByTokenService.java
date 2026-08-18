package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ConfirmConvocationByCodeCommand;
import com.architek.oikos.meeting.application.command.ConfirmConvocationByTokenCommand;
import com.architek.oikos.meeting.application.dto.ConvocationConfirmationView;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.ConfirmConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByCodeUseCase;
import com.architek.oikos.meeting.application.port.in.GetConvocationByTokenUseCase;
import com.architek.oikos.meeting.application.port.out.ConfirmationAttemptLimiterPort;
import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.application.query.GetConvocationByCodeQuery;
import com.architek.oikos.meeting.application.query.GetConvocationByTokenQuery;
import com.architek.oikos.meeting.domain.exception.ConfirmationClosedException;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.exception.InvalidConfirmationCodeException;
import com.architek.oikos.meeting.domain.exception.InvalidConvocationTokenException;
import com.architek.oikos.meeting.domain.exception.TooManyConfirmationAttemptsException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.ConvocationReply;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ConvocationReplyId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;

/**
 * The confirmation link received with the convocation: what it shows, and what
 * it records.
 *
 * <p>It exists because a large share of lots have no account behind them. Their
 * owners receive the convocation and, until now, had no way of answering other
 * than telephoning the syndic - which is why every reply in the system reached
 * the office some other way and was recorded as {@link ReplySource#OTHER}. The
 * link is what makes {@link ReplySource#OWNER_LINK} a real answer rather than a
 * hypothetical one.
 *
 * <p>Both entry points are anonymous, which shapes the whole class:
 *
 * <ul>
 * <li>The token is the only thing looked up. An unknown one yields the same
 * flat "not valid" for every wrong token - saying more would turn the endpoint
 * into an oracle. It is the whole credential for <em>reading</em> the page;
 * recording an answer also takes the lot's six-character code (ADR 0002
 * §16).</li>
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
 *
 * <p>The same two operations are also reachable by the pair of six-character
 * codes printed on the letter, for whoever will not type a 43-character token.
 * That path is deliberately narrower than the token one, and the difference is
 * the whole reason it can exist at all: the code is scoped to one meeting (so a
 * guess is bounded to one copropriété's lots rather than every convocation ever
 * issued), and wrong attempts are capped per caller. A short secret is only
 * defensible when it cannot be tried in a loop - ADR 0002 §13.
 */
@Component
public class ConvocationByTokenService implements GetConvocationByTokenUseCase, ConfirmConvocationByTokenUseCase,
        GetConvocationByCodeUseCase, ConfirmConvocationByCodeUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConvocationByTokenService.class);

    private final ConvocationRepository convocationRepository;
    private final GeneralMeetingRepository generalMeetingRepository;
    private final PropertyDirectoryPort propertyDirectoryPort;
    private final ConvocationViewAssembler viewAssembler;
    private final ConfirmationAttemptLimiterPort attemptLimiter;
    private final Clock clock;

    public ConvocationByTokenService(ConvocationRepository convocationRepository,
                                      GeneralMeetingRepository generalMeetingRepository,
                                      PropertyDirectoryPort propertyDirectoryPort,
                                      ConvocationViewAssembler viewAssembler,
                                      ConfirmationAttemptLimiterPort attemptLimiter, Clock clock) {
        this.convocationRepository = convocationRepository;
        this.generalMeetingRepository = generalMeetingRepository;
        this.propertyDirectoryPort = propertyDirectoryPort;
        this.viewAssembler = viewAssembler;
        this.attemptLimiter = attemptLimiter;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocationConfirmationView getByToken(GetConvocationByTokenQuery query) {
        Convocation convocation = require(query.token());
        return toView(convocation, requireMeetingOf(convocation));
    }

    /**
     * Reading the page takes the link; answering takes the link <em>and</em> the
     * lot's code.
     *
     * <p>The token says the convocation was received, never by whom. It is
     * forwarded, printed, left on a table, and it opens a page whose two buttons
     * would otherwise let anyone holding it answer in the lot's name. The code
     * asks for the letter itself, which is what the person answering for the lot
     * has in hand (ADR 0002 §16).
     */
    @Override
    @Transactional
    public ConvocationConfirmationView confirm(ConfirmConvocationByTokenCommand command) {
        Convocation convocation = require(command.token());
        GeneralMeeting meeting = requireMeetingOf(convocation);
        requireCodeOf(convocation, meeting, command.confirmationCode(), command.callerId());
        return record(convocation, meeting, command.attendanceReply());
    }

    /**
     * The code, checked against the convocation the token already found - and
     * capped exactly like the paper path.
     *
     * <p>The cap is not decoration here either: without it, a leaked link would
     * turn into six characters to walk, and the link is the very thing whose
     * leaking this check exists to survive. Same key as the paper path, so
     * attempts on one meeting count together whichever door they come through.
     *
     * <p>A wrong code is named as such, unlike a wrong pair of codes. Nothing is
     * given away by it: whoever holds the link is already looking at the lot the
     * page names, and telling them "this link is not valid" would send someone
     * who mistyped one character looking for a new convocation.
     */
    private void requireCodeOf(Convocation convocation, GeneralMeeting meeting, ShortCode submitted,
                                String callerId) {
        String limiterKey = meeting.getPublicReference().value();
        if (!attemptLimiter.isAllowed(limiterKey, callerId)) {
            throw new TooManyConfirmationAttemptsException();
        }
        if (submitted == null || !submitted.equals(convocation.getConfirmationCode())) {
            attemptLimiter.recordFailure(limiterKey, callerId);
            throw new InvalidConfirmationCodeException();
        }
        attemptLimiter.recordSuccess(limiterKey, callerId);
    }

    /**
     * Shared by both anonymous paths: the answer is the same fact and carries the
     * same source, whether the person followed a link or typed six characters.
     */
    private ConvocationConfirmationView record(Convocation convocation, GeneralMeeting meeting,
                                                AttendanceReply reply) {
        if (!isOpenForConfirmation(meeting)) {
            throw new ConfirmationClosedException();
        }
        // OWNER_LINK even on a withdrawal: the history entry records who performed the act, and
        // somebody holding the link did. No medium - the answer came through the application,
        // not to the office by some other means.
        // No announced mode and no stand-in: the public confirmation page asks for neither,
        // and inventing "sur place" for someone who only clicked a link would be a fabrication.
        Convocation answered = convocation.reply(ConvocationReply.record(ConvocationReplyId.newId(), reply, null,
                false, ReplySource.OWNER_LINK, null, null, null, clock.instant(), null));
        // The lot, not the person: nothing here identifies who answered, and the log must not
        // pretend otherwise.
        log.info("Convocation {} answered {} through its confirmation link", convocation.getId(), reply);
        return toView(convocationRepository.save(answered), meeting);
    }

    @Override
    @Transactional(readOnly = true)
    public ConvocationConfirmationView getByCode(GetConvocationByCodeQuery query) {
        Convocation convocation = requireByCode(query.meetingReference(), query.confirmationCode(), query.callerId());
        return toView(convocation, requireMeetingOf(convocation));
    }

    @Override
    @Transactional
    public ConvocationConfirmationView confirm(ConfirmConvocationByCodeCommand command) {
        Convocation convocation = requireByCode(command.meetingReference(), command.confirmationCode(),
                command.callerId());
        return record(convocation, requireMeetingOf(convocation), command.attendanceReply());
    }

    /**
     * The convocation behind a pair of codes, or the same flat refusal every
     * wrong pair gets.
     *
     * <p>An unknown meeting reference and an unknown code are indistinguishable
     * on purpose: telling them apart would let someone confirm a reference for
     * free and then spend every attempt on the code alone.
     *
     * <p>The cap is checked before the lookup and fed after it. Note that a
     * wrong reference counts too - otherwise walking references would be
     * unlimited, and the reference is what bounds the code space.
     */
    private Convocation requireByCode(ShortCode meetingReference, ShortCode confirmationCode, String callerId) {
        if (meetingReference == null || confirmationCode == null) {
            throw new InvalidConvocationTokenException();
        }
        String limiterKey = meetingReference.value();
        if (!attemptLimiter.isAllowed(limiterKey, callerId)) {
            throw new TooManyConfirmationAttemptsException();
        }

        Optional<Convocation> found = generalMeetingRepository.findByPublicReference(meetingReference)
                .flatMap(meeting -> convocationRepository
                        .findByGeneralMeetingIdAndConfirmationCode(meeting.getId(), confirmationCode));
        if (found.isEmpty()) {
            attemptLimiter.recordFailure(limiterKey, callerId);
            throw new InvalidConvocationTokenException();
        }
        attemptLimiter.recordSuccess(limiterKey, callerId);
        return found.get();
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
