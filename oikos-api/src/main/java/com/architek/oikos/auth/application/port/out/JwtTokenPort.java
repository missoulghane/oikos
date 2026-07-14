package com.architek.oikos.auth.application.port.out;

import java.util.Set;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port hiding the concrete JWT implementation (JJWT / HMAC HS256) from the
 * application layer. Implemented by auth.infrastructure.security.JwtService.
 */
public interface JwtTokenPort {

    String generateAccessToken(EntityId userId, Set<String> authorities);

    long accessTokenTtlSeconds();
}
