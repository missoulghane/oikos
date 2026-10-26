package com.architek.oikos.user.web.response;

/**
 * Reply to step 2 of the volunteer-syndic wizard. onboardingToken lets the
 * wizard post its configuration (steps 3 to 7) before the account is verified;
 * it is scoped to propertyId and to that single endpoint - see
 * JwtService.generateOnboardingToken.
 */
public record BoardAdminPropertyResponse(String propertyId, String onboardingToken, long expiresInSeconds) {
}
