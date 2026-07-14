package com.architek.oikos.auth.application.dto;

public record AuthTokens(String accessToken, String refreshToken, long expiresInSeconds) {
}
