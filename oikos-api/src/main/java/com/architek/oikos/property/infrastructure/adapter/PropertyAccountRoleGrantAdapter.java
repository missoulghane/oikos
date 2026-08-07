package com.architek.oikos.property.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.port.out.AccountRoleGrantPort;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.GrantPropertyRoleCommand;
import com.architek.oikos.user.application.port.in.GrantPropertyRoleUseCase;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Cross-feature adapter: delegates to user's public port-in use case
 * (GrantPropertyRoleUseCase), never to user's repository directly (rule 6).
 * The only place property's raw targetRole String is resolved back into
 * user's own PropertyRole enum, right before calling into user's port - same
 * pattern as invitation.infrastructure.adapter.InvitationAccountDirectoryAdapter.
 */
@Component
public class PropertyAccountRoleGrantAdapter implements AccountRoleGrantPort {

    private final GrantPropertyRoleUseCase grantPropertyRoleUseCase;

    public PropertyAccountRoleGrantAdapter(GrantPropertyRoleUseCase grantPropertyRoleUseCase) {
        this.grantPropertyRoleUseCase = grantPropertyRoleUseCase;
    }

    @Override
    public void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole) {
        grantPropertyRoleUseCase.grant(new GrantPropertyRoleCommand(
                UserId.of(userId.value()), partyId, propertyId, PropertyRole.valueOf(targetRole)));
    }
}
