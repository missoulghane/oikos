package com.architek.oikos.auth.domain.repository;

import java.util.Optional;

import com.architek.oikos.auth.domain.model.PasswordResetToken;
import com.architek.oikos.shared.domain.valueobject.EntityId;

public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(EntityId userId);
}
