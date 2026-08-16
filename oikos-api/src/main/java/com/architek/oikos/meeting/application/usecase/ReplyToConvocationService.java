package com.architek.oikos.meeting.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.command.ReplyToConvocationCommand;
import com.architek.oikos.meeting.application.dto.ConvocationView;
import com.architek.oikos.meeting.application.port.in.ReplyToConvocationUseCase;
import com.architek.oikos.meeting.application.port.out.OwnerInfo;
import com.architek.oikos.meeting.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.meeting.application.port.out.UnitInfo;
import com.architek.oikos.meeting.domain.model.Convocation;
import com.architek.oikos.meeting.domain.valueobject.AttendanceReply;
import com.architek.oikos.meeting.domain.valueobject.ReplySource;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * The owner answering, or the syndic entering the answer on their behalf - the
 * same use case for both, because a lot whose owners have no account still has
 * to be tracked. Who may call it is an authorization question
 * (PropertyAccessEvaluator.canReplyToConvocation), not a business one.
 *
 * <p>What this service adds beyond storing the answer is <em>how it was
 * obtained</em>. The source is deduced here, from whether the caller happens to
 * own the lot, and is never read from the request: a client able to state its
 * own source could write "the copropriétaire confirmed from the app" about an
 * answer nobody gave, which is exactly the assertion a contested meeting turns
 * on. OWNER_LINK is not reachable from here - it belongs to the tokenised
 * confirmation link, which authenticates nobody and so cannot go through an
 * authenticated endpoint.
 */
@Component
public class ReplyToConvocationService implements ReplyToConvocationUseCase {

    private final ConvocationLookup lookup;
    private final PartyAccountDirectoryPort partyAccountDirectoryPort;
    private final Clock clock;

    public ReplyToConvocationService(ConvocationLookup lookup, PartyAccountDirectoryPort partyAccountDirectoryPort,
                                      Clock clock) {
        this.lookup = lookup;
        this.partyAccountDirectoryPort = partyAccountDirectoryPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ConvocationView reply(ReplyToConvocationCommand command) {
        Convocation convocation = lookup.require(command.convocationId());
        UnitInfo unit = lookup.unitOf(convocation);

        EntityId answeringParty = command.attendanceReply() == AttendanceReply.NO_REPLY ? null
                : ownerPartyOf(unit, command.requestedByUserId());
        ReplySource source = command.attendanceReply() == AttendanceReply.NO_REPLY ? null
                : answeringParty != null ? ReplySource.OWNER_APP : ReplySource.SYNDIC_OFFICE;

        return lookup.save(convocation.reply(command.attendanceReply(), source, answeringParty, command.note(),
                clock.instant()), unit);
    }

    /**
     * The lot's owner behind the calling account, or null when the caller is
     * not one - which is what tells an owner answering for themselves from a
     * syndic entering an answer they were given.
     *
     * <p>Resolved through the account each owner party is linked to, rather
     * than the other way round: this module never learns what a UnitOwnership
     * is (rule 4), and the port it does have answers exactly this question.
     */
    private EntityId ownerPartyOf(UnitInfo unit, EntityId callerUserId) {
        if (unit == null || callerUserId == null || unit.owners().isEmpty()) {
            return null;
        }
        List<EntityId> partyIds = unit.owners().stream().map(OwnerInfo::partyId).toList();
        Map<EntityId, EntityId> userIdsByParty = partyAccountDirectoryPort.resolveUserIds(partyIds);
        return userIdsByParty.entrySet().stream().filter(entry -> callerUserId.equals(entry.getValue()))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }
}
