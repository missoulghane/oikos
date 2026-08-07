package com.architek.oikos.user.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Lets RegisterUserService submit a PUBLIC-invitation membership request as
 * part of account creation itself, so the request is visible to the property
 * manager immediately - not only once the new user verifies their email and
 * logs back in. Submission is idempotent on the invitation module's side, so
 * the post-login auto-confirm step in the invitation wizard safely retries
 * the same call without creating a duplicate.
 */
public interface MembershipRequestSubmissionPort {

    void submit(String invitationToken, UserId userId, EntityId unitId);
}
