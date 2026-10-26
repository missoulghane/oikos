package com.architek.oikos.user.web.response;

/**
 * Reply to step 1 of the volunteer-syndic wizard. onboardingToken lets the
 * wizard create the account's property at step 2 before the account is
 * verified; it is scoped to that single endpoint - see
 * JwtService.generateAccountOnboardingToken.
 */
public record RegisteredBoardAdminResponse(String userId, String onboardingToken, long expiresInSeconds) {
}
