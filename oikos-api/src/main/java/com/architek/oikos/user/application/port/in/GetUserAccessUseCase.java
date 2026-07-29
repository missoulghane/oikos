package com.architek.oikos.user.application.port.in;

import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.query.GetUserAccessQuery;

/**
 * Public entry point used by cross-feature authorization checks (e.g.
 * PropertyAccessEvaluator in the auth feature) to resolve the current
 * caller's access rights. Cross-feature access must go through this
 * port-in use case, never through the user repository directly (rule 6).
 */
public interface GetUserAccessUseCase {

    UserAccessView getAccess(GetUserAccessQuery query);
}
