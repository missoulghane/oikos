package com.architek.oikos.messaging.infrastructure.adapter;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.architek.oikos.messaging.application.port.out.UserAccessPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.application.port.in.GetUserAccessUseCase;
import com.architek.oikos.user.application.query.GetUserAccessQuery;
import com.architek.oikos.user.domain.model.Permission;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Cross-feature adapter: delegates to user's public port-in
 * (GetUserAccessUseCase), never to user's repository directly (rule 6) - the
 * same use case PropertyAccessEvaluator itself already relies on. Named with
 * the "Messaging" prefix to avoid a bean collision with other modules'
 * equivalent adapters (pattern already used by
 * AccountingPropertyDirectoryAdapter/InvitationAccountDirectoryAdapter).
 */
@Component
public class MessagingUserAccessAdapter implements UserAccessPort {

    private final GetUserAccessUseCase getUserAccessUseCase;

    public MessagingUserAccessAdapter(GetUserAccessUseCase getUserAccessUseCase) {
        this.getUserAccessUseCase = getUserAccessUseCase;
    }

    @Override
    public Set<EntityId> memberPropertyIds(EntityId userId) {
        return access(userId).rolesByProperty().keySet().stream().map(EntityId::of).collect(Collectors.toSet());
    }

    @Override
    public boolean isMember(EntityId userId, EntityId propertyId) {
        return access(userId).rolesByProperty().containsKey(propertyId.toString());
    }

    @Override
    public boolean canBroadcast(EntityId userId, EntityId propertyId) {
        return access(userId).hasPermission(propertyId.toString(), Permission.MESSAGING_BROADCAST);
    }

    private UserAccessView access(EntityId userId) {
        return getUserAccessUseCase.getAccess(new GetUserAccessQuery(UserId.of(userId.value())));
    }
}
