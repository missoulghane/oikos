package com.architek.oikos.meeting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.architek.oikos.meeting.domain.model.Vote;
import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.meeting.domain.valueobject.VoteChoice;
import com.architek.oikos.meeting.domain.valueobject.VoteId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * One line of the nominal vote sheet. unitNumber and votingWeight come from
 * the lot's convocation, resolved by the caller: the vote itself holds neither
 * (see Vote's javadoc on why the weight is not stored twice).
 */
public record VoteView(VoteId id, AgendaItemId agendaItemId, EntityId unitId, String unitNumber, String buildingName,
                        BigDecimal votingWeight, VoteChoice choice, Instant castAt) {

    public static VoteView from(Vote vote, String unitNumber, String buildingName, BigDecimal votingWeight) {
        return new VoteView(vote.getId(), vote.getAgendaItemId(), vote.getUnitId(), unitNumber, buildingName,
                votingWeight, vote.getChoice(), vote.getCastAt());
    }
}
