package com.architek.oikos.auth.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.auth.application.port.out.UserAccountPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.application.port.in.LoadUserByIdentifierUseCase;
import com.architek.oikos.user.application.port.in.OverwritePasswordUseCase;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Cross-feature adapter: delegates to user's public port-in use cases
 * (LoadUserByIdentifierUseCase, OverwritePasswordUseCase), never to user's repository
 * directly (rule 6).
 */
@Component
public class UserAccountAdapter implements UserAccountPort {

    private final LoadUserByIdentifierUseCase loadUserByIdentifierUseCase;
    private final OverwritePasswordUseCase overwritePasswordUseCase;

    public UserAccountAdapter(LoadUserByIdentifierUseCase loadUserByIdentifierUseCase,
                               OverwritePasswordUseCase overwritePasswordUseCase) {
        this.loadUserByIdentifierUseCase = loadUserByIdentifierUseCase;
        this.overwritePasswordUseCase = overwritePasswordUseCase;
    }

    @Override
    public Optional<EntityId> findIdByEmail(EmailVO email) {
        return loadUserByIdentifierUseCase.loadByIdentifier(email.value()).map(User::getId).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public void overwritePassword(EntityId userId, HashedPassword newPassword) {
        overwritePasswordUseCase.overwritePassword(UserId.of(userId.value()), newPassword);
    }
}
