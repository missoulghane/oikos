package com.architek.oikos.user.web.response;

import com.architek.oikos.user.application.dto.RegisteredBoardAdminView;

/**
 * Reply to step 2 of the volunteer-syndic wizard. onboardingToken lets the
 * wizard post its configuration (steps 3 to 7) before the account is verified;
 * it is scoped to propertyId and to that single endpoint - see
 * JwtService.generateOnboardingToken.
 */
public record RegisteredBoardAdminResponse(String userId, String propertyId, String onboardingToken,
                                            long expiresInSeconds) {

    public static RegisteredBoardAdminResponse from(RegisteredBoardAdminView view, String onboardingToken,
                                                     long expiresInSeconds) {
        return new RegisteredBoardAdminResponse(view.userId().toString(), view.propertyId().toString(),
                onboardingToken, expiresInSeconds);
    }
}
