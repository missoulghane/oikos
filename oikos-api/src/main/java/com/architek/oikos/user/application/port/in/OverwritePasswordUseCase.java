package com.architek.oikos.user.application.port.in;

import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Public entry point used by the auth feature (password-reset flow) to force-set a
 * user's password without checking the previous one - the caller must already have
 * validated a one-time reset token. Cross-feature access must go through this
 * port-in use case, never through the user repository directly.
 */
public interface OverwritePasswordUseCase {

    void overwritePassword(UserId userId, HashedPassword newPassword);
}
