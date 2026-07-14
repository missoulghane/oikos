package com.architek.oikos.auth.application.port.out;

import com.architek.oikos.auth.application.dto.AuthenticatedPrincipal;
import com.architek.oikos.shared.domain.valueobject.RawPassword;

/**
 * Bridge to Spring Security: verifies credentials and reports back the authenticated
 * principal's id and authorities. Throws UnauthorizedException on bad credentials or
 * an unverified account. identifier can be the account's own login, or the
 * email/phone of its linked contact (resolution priority owned by
 * user.application.usecase.LoadUserByIdentifierService).
 */
public interface AuthenticationPort {

    AuthenticatedPrincipal authenticate(String identifier, RawPassword rawPassword);
}
