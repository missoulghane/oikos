package com.architek.oikos.auth.web.response;

import com.architek.oikos.auth.application.dto.AuthTokens;

public record AuthResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static AuthResponse from(AuthTokens tokens) {
        return new AuthResponse(tokens.accessToken(), tokens.refreshToken(), "Bearer", tokens.expiresInSeconds());
    }
}
