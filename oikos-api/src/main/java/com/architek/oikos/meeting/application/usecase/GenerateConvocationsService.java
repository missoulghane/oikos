package com.architek.oikos.meeting.application.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.GenerateConvocationsCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.GenerateConvocationsUseCase;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.exception.GeneralMeetingNotFoundException;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.meeting.domain.repository.ConvocationRepository;
import com.architek.oikos.meeting.domain.service.ConvocationTokenGenerator;
import com.architek.oikos.meeting.domain.service.ShortCodeGenerator;
import com.architek.oikos.meeting.domain.valueobject.ShortCode;
import com.architek.oikos.meeting.domain.repository.GeneralMeetingRepository;
import com.architek.oikos.meeting.domain.valueobject.ConvocationId;
import com.architek.oikos.meeting.domain.valueobject.MeetingStatus;
import com.architek.oikos.meeting.domain.valueobject.VotingWeight;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Creates one convocation per lot of the copropriété and moves the meeting to
 * CONVENED.
 *
 * <p>Per LOT, and for every lot - including those with no owner recorded. Such
 * a lot cannot answer or sign in, but it exists, it weighs in the total the
 * quorum and an absolute majority are measured against, and leaving it out
 * would quietly lower both bars (ADR 0002 §2).
 *
 * <p>Find-or-create by (meeting, lot): the endpoint is idempotent, so a double
 * click or a retry after a timeout adds nothing. That also lets it be re-run
 * after a lot is added to the copropriété between two attempts - the new lot
 * gets its convocation, the existing ones keep theirs, weight snapshot
 * included.
 *
 * <p>Generating sends nothing. It creates the rows, files nothing, and moves
 * the meeting to CONVENED; the syndic then reviews the tracking table and
 * sends when ready (SendPendingConvocationsUseCase). Welding the two together
 * meant a single button whose failure mode - "some emails went out, some
 * didn't, and the meeting is convened either way" - was impossible to explain.
 */
@Component
public class GenerateConvocationsService implements GenerateConvocationsUseCase {

    private static final Logger log = LoggerFactory.getLogger(GenerateConvocationsService.class);

    private static final int MAX_CODE_ATTEMPTS = 10;

    private final GeneralMeetingRepository generalMeetingRepository;
    private final ConvocationRepository convocationRepository;
    private final ConvocationViewAssembler viewAssembler;
    private final ConvocationTokenGenerator tokenGenerator;
    private final ShortCodeGenerator shortCodeGenerator;

    public GenerateConvocationsService(GeneralMeetingRepository generalMeetingRepository,
                                        ConvocationRepository convocationRepository,
                                        ConvocationViewAssembler viewAssembler,
                                        ConvocationTokenGenerator tokenGenerator,
                                        ShortCodeGenerator shortCodeGenerator) {
        this.generalMeetingRepository = generalMeetingRepository;
        this.convocationRepository = convocationRepository;
        this.viewAssembler = viewAssembler;
        this.tokenGenerator = tokenGenerator;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    /**
     * A code free within this meeting. Uniqueness is only ever per meeting - two
     * copropriétés may both hand out `w754a1`, since the meeting's public
     * reference is always presented alongside.
     *
     * <p>Bounded rather than a while(true): with a few hundred lots against
     * 34^6, ten failures in a row means the generator is broken, not unlucky.
     */
    private ShortCode drawUnusedCode(Set<ShortCode> usedCodes) {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            ShortCode candidate = ShortCode.of(shortCodeGenerator.generate());
            if (!usedCodes.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not draw a free confirmation code in " + MAX_CODE_ATTEMPTS
                + " attempts");
    }

    @Override
    @Transactional
    public List<ConvocationView> generate(GenerateConvocationsCommand command) {
        GeneralMeeting meeting = generalMeetingRepository.findById(command.generalMeetingId())
                .orElseThrow(() -> new GeneralMeetingNotFoundException(command.generalMeetingId()));

        Map<EntityId, UnitInfo> unitsById = viewAssembler.unitsByIdOf(meeting.getPropertyId());
        Set<EntityId> alreadyConvoked = convocationRepository
                .findByGeneralMeetingIdAndUnitIds(meeting.getId(), unitsById.keySet()).stream()
                .map(Convocation::getUnitId).collect(Collectors.toSet());

        // The codes already handed out for this meeting, so a re-run for a lot added late
        // cannot draw one twice. Held in memory for the whole loop rather than queried per
        // lot: the codes being created right now are not in the table yet either.
        Set<ShortCode> usedCodes = new HashSet<>(convocationRepository.findConfirmationCodes(meeting.getId()));

        List<Convocation> created = new ArrayList<>();
        for (UnitInfo unit : unitsById.values()) {
            if (alreadyConvoked.contains(unit.unitId())) {
                continue;
            }
            // Both secrets are minted here rather than at send time: they are printed on the
            // letter, including the one a syndic prints to post.
            ShortCode code = drawUnusedCode(usedCodes);
            usedCodes.add(code);
            created.add(Convocation.generate(ConvocationId.newId(), meeting.getId(), unit.unitId(),
                    VotingWeight.forMode(meeting.getVotingWeightMode(), unit.shares()),
                    tokenGenerator.generate(), code));
        }
        convocationRepository.saveAll(created);

        // Only on the first run: a re-run for a lot added late must not push an already
        // convened meeting through a transition it has left behind.
        if (meeting.getStatus() == MeetingStatus.SCHEDULED) {
            generalMeetingRepository.save(meeting.convene());
        }

        log.info("Generated {} convocation(s) for general meeting {} ({} lot(s) already convoked)", created.size(),
                meeting.getId(), alreadyConvoked.size());

        return viewAssembler.toViews(convocationRepository.findByGeneralMeetingId(meeting.getId()), unitsById);
    }
}
