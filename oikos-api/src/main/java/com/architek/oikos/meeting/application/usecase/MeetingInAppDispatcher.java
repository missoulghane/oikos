package com.architek.oikos.meeting.application.usecase;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.meeting.application.port.out.MeetingMessagingPort;
import com.architek.oikos.meeting.application.port.out.MeetingNotificationPort;
import com.architek.oikos.meeting.application.port.out.PartyAccountDirectoryPort;
import com.architek.oikos.meeting.domain.model.GeneralMeeting;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Everything this module delivers inside the application - the messagerie and
 * the notification bell - and the one place their transaction is isolated.
 *
 * <p>It exists for one reason: to make "best effort" actually true.
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
public class MeetingInAppDispatcher {

    private final PartyAccountDirectoryPort partyAccountDirectoryPort;
    private final MeetingNotificationPort notificationPort;
    private final MeetingMessagingPort messagingPort;

    public MeetingInAppDispatcher(PartyAccountDirectoryPort partyAccountDirectoryPort,
                                   MeetingNotificationPort notificationPort, MeetingMessagingPort messagingPort) {
        this.partyAccountDirectoryPort = partyAccountDirectoryPort;
        this.notificationPort = notificationPort;
        this.messagingPort = messagingPort;
    }

    /**
     * Delivers one lot's convocation inside the application - a message in the
     * messagerie, and a notification pointing at it - and returns how many
     * account holders it reached.
     *
     * <p><b>Two acts, one delivery.</b> The caller records a single row for
     * both, and that is deliberate: ADR 0002 §9 refused a second channel for
     * the mobile app on exactly this ground, that two rows would count two
     * sends for one act and show 120 envois for 60 lots. The message is the
     * content, the notification is the signal that it arrived.
     *
     * <p><b>The message first, then the notification.</b> A notification
     * pointing at a thread that failed to post is worse than no notification:
     * it sends the copropriétaire looking for something that is not there. In
     * that order, a failing message means neither goes out and the whole
     * delivery is recorded FAILED.
     *
     * <p>The count is not decoration. The in-app delivery IS the delivery on
     * this channel rather than a courtesy alongside the email, so "nobody has
     * an account" has to come back as a fact the caller can record as FAILED,
     * not vanish into a loop that ran zero times.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int deliverConvocation(Collection<EntityId> partyIds, GeneralMeeting meeting, EntityId senderUserId,
                                   String subject, String body, String lotLabel) {
        Collection<EntityId> recipients = accountsOf(partyIds);
        if (recipients.isEmpty()) {
            return 0;
        }
        messagingPort.postConvocation(new MeetingMessagingPort.ConvocationMessage(meeting.getPropertyId(),
                senderUserId, Set.copyOf(recipients), subject, body, lotLabel));
        for (EntityId userId : recipients) {
            notificationPort.notifyGeneralMeetingCalled(userId, meeting.getPropertyId(), meeting.getTitle(),
                    linkTo(meeting));
        }
        return recipients.size();
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
     *
     * <p>And deliberately not the confirmation link either, although that is
     * where the notification is meant to lead. The link carries a bearer token
     * for people who have no account; everyone reached here has one, and this
     * route is where they confirm as themselves. Answering through it records
     * OWNER_APP and names the responder, where the token records OWNER_LINK and
     * cannot say which indivisaire clicked (ADR 0002 §10). Sending the secret to
     * someone already authenticated would spread it for weaker evidence.
     */
    private static String linkTo(GeneralMeeting meeting) {
        return "/property-ownership/general-meetings/" + meeting.getId();
    }
}
