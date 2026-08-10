package com.architek.oikos.messaging.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

/**
 * Thrown by StartGroupConversationService when the sender of a would-be
 * GROUP conversation is not currently a member of the given property, when
 * a recipient is not a current member with a linked account, when the
 * recipient list is empty, or when the caller targets themselves as a
 * recipient - a caller is never their own GROUP conversation recipient,
 * same rationale as "not a property member" since there is no valid
 * recipient in either case.
 */
public class RecipientNotPropertyMemberException extends BusinessException {

    public RecipientNotPropertyMemberException(String message) {
        super(message);
    }
}
