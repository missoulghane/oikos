package com.architek.oikos.user.domain.repository;

import java.util.Optional;

import com.architek.oikos.user.domain.model.VerificationToken;
import com.architek.oikos.user.domain.valueobject.UserId;

public interface VerificationTokenRepository {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findByToken(String token);

    void deleteByUserId(UserId userId);
}
