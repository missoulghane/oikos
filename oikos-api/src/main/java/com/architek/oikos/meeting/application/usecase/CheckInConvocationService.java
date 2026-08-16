package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.CheckInConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.CheckInConvocationUseCase;

/**
 * Signing a lot in at the opening of the session. This is what lets it vote,
 * and what makes its weight count towards the quorum - an owner who confirmed
 * they would attend and then did not come changes neither.
 *
 * <p>Deliberately not gated on the meeting being IN_PROGRESS: the sign-in
 * sheet is filled as people arrive, which is before the chair opens the
 * session, and the quorum can only be assessed once it is. Gating it would
 * make opening impossible.
 */
@Component
public class CheckInConvocationService implements CheckInConvocationUseCase {

    private final ConvocationLookup lookup;
    private final Clock clock;

    public CheckInConvocationService(ConvocationLookup lookup, Clock clock) {
        this.lookup = lookup;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConvocationView checkIn(CheckInConvocationCommand command) {
        return lookup.save(lookup.require(command.convocationId())
                .checkIn(command.attendanceMode(), command.checkedInPartyId(), clock.instant()));
    }

    @Override
    @Transactional
    public ConvocationView undoCheckIn(CheckInConvocationCommand command) {
        return lookup.save(lookup.require(command.convocationId()).undoCheckIn());
    }
}
