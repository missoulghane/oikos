package com.architek.oikos.meeting.application.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.port.out.MeetingNotificationPort;
import com.architek.oikos.meeting.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Sends the module's in-app notifications, and exists for one reason: to make
 * "best effort" actually true.
 *
 * <p>Both call sites used to do this inline, inside their own transaction,
 * wrapped in a try/catch that looked like it made failure harmless. It did not.
 * The two ports crossed here reach services annotated {@code @Transactional}
 * with the default REQUIRED propagation - FindUsersByPartyIdsService and
 * CreateNotificationService - so they JOIN the caller's transaction. When one of
 * them throws, Spring marks that shared transaction rollback-only
 * (globalRollbackOnParticipationFailure, true by default) before the exception
 * ever reaches the catch. Swallowing it changes nothing: the commit then fails
 * with UnexpectedRollbackException, and the whole convocation is lost - recorded
 * as still waiting to be sent, with the email already gone.
 *
 * <p>REQUIRES_NEW is what fixes it: it SUSPENDS the caller's transaction, so a
 * failure in here marks only this one. The callers keep their try/catch, which
 * finally does what it claims.
 *
 * <p>This has to live on its own bean. Annotating the private methods the
 * callers used to have would have changed nothing at all - self-invocation does
 * not go through the proxy - and would have been worse than the bug, being a bug
 * under an annotation asserting the opposite.
 *
 * <p>One consequence worth knowing: a notification committed in its own
 * transaction survives even if the caller's own work later rolls back. Here that
 * introduces no inconsistency the flow does not already have - the convocation
 * email is sent over SMTP well before the commit, and cannot be unsent either.
 */
@Component
public class MeetingNotificationDispatcher {

    private final PartyAccountDirectoryPort partyAccountDirectoryPort;
    private final MeetingNotificationPort notificationPort;

    public MeetingNotificationDispatcher(PartyAccountDirectoryPort partyAccountDirectoryPort,
                                          MeetingNotificationPort notificationPort) {
        this.partyAccountDirectoryPort = partyAccountDirectoryPort;
        this.notificationPort = notificationPort;
    }

    /** Tells the owners who have an account that their lot has been convoked. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyConvoked(Collection<EntityId> partyIds, GeneralMeeting meeting) {
        for (EntityId userId : accountsOf(partyIds)) {
            notificationPort.notifyGeneralMeetingCalled(userId, meeting.getPropertyId(), meeting.getTitle(),
                    linkTo(meeting));
        }
    }

    /** Tells them the minutes of that meeting are out. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyMinutesPublished(Collection<EntityId> partyIds, GeneralMeeting meeting) {
        for (EntityId userId : accountsOf(partyIds)) {
            notificationPort.notifyMinutesPublished(userId, meeting.getPropertyId(), meeting.getTitle(),
                    linkTo(meeting));
        }
    }

    private Collection<EntityId> accountsOf(Collection<EntityId> partyIds) {
        if (partyIds.isEmpty()) {
            return List.of();
        }
        Map<EntityId, EntityId> userIdsByParty = partyAccountDirectoryPort.resolveUserIds(partyIds);
        return userIdsByParty.values();
    }

    /**
     * The owner-space route, on both clients: oikos-web serves it and
     * oikos-mobile navigates on the same prefix. It is deliberately not the
     * back-office path - the recipient is a copropriétaire.
     */
    private static String linkTo(GeneralMeeting meeting) {
        return "/property-ownership/general-meetings/" + meeting.getId();
    }
}
