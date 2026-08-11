package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.messaging.domain.model.SenderIdentity;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.exception.UnauthorizedException;

/**
 * Resolves which hat a GROUP message is actually posted under (the only
 * conversation type where the choice is ever ambiguous - BOARD_PRIVATE and
 * BROADCAST always force BOARD instead of asking, see SendMessageService).
 * A sender can never self-declare a hat they don't hold on the property:
 * OWNER requires PROPERTY_OWNER, BOARD requires a staff role (board or
 * manager, admin or member tier). When the client sends no explicit choice
 * (claimedIdentity == null), it is only accepted if the sender is eligible
 * for exactly one of the two - composing "as X" is only ever a real decision
 * for an account holding both roles on this property (case 2/4/6/7), so a
 * single-role sender is never forced to answer a question with one option.
 */
@Component
class SenderIdentityValidator {

    private final UserAccessPort userAccessPort;

    SenderIdentityValidator(UserAccessPort userAccessPort) {
        this.userAccessPort = userAccessPort;
    }

    SenderIdentity resolve(EntityId userId, EntityId propertyId, SenderIdentity claimedIdentity) {
        if (claimedIdentity != null) {
            requireEligible(userId, propertyId, claimedIdentity);
            return claimedIdentity;
        }

        boolean owner = userAccessPort.ownsProperty(userId, propertyId);
        boolean board = userAccessPort.managesProperty(userId, propertyId);
        if (owner && !board) {
            return SenderIdentity.OWNER;
        }
        if (board && !owner) {
            return SenderIdentity.BOARD;
        }
        throw new UnauthorizedException(
                "senderIdentity is required: sender holds both OWNER and BOARD roles on property " + propertyId);
    }

    private void requireEligible(EntityId userId, EntityId propertyId, SenderIdentity claimedIdentity) {
        boolean eligible = claimedIdentity == SenderIdentity.BOARD
                ? userAccessPort.managesProperty(userId, propertyId)
                : userAccessPort.ownsProperty(userId, propertyId);
        if (!eligible) {
            throw new UnauthorizedException(
                    "sender does not hold the " + claimedIdentity + " identity on property " + propertyId);
        }
    }
}
