package com.architek.oikos.invitation.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.AccountDirectoryPort;
import com.architek.oikos.invitation.application.port.out.AccountInfo;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.command.GrantPropertyRoleCommand;
import com.architek.oikos.user.application.dto.UserView;
import com.architek.oikos.user.application.port.in.GetUserUseCase;
import com.architek.oikos.user.application.port.in.GrantPropertyRoleUseCase;
import com.architek.oikos.user.application.query.GetUserQuery;
import com.architek.oikos.user.domain.model.PropertyRole;
import com.architek.oikos.user.domain.valueobject.UserId;

/**
 * Cross-feature adapter: delegates to user's public port-in use cases
 * (GetUserUseCase, GrantPropertyRoleUseCase), never to user's repository
 * directly (rule 6). The only place invitation's stored targetRole String is
 * resolved back into user's own PropertyRole enum, right before calling into
 * user's port.
 */
@Component
public class InvitationAccountDirectoryAdapter implements AccountDirectoryPort {

    private final GetUserUseCase getUserUseCase;
    private final GrantPropertyRoleUseCase grantPropertyRoleUseCase;

    public InvitationAccountDirectoryAdapter(GetUserUseCase getUserUseCase, GrantPropertyRoleUseCase grantPropertyRoleUseCase) {
        this.getUserUseCase = getUserUseCase;
        this.grantPropertyRoleUseCase = grantPropertyRoleUseCase;
    }

    @Override
    public AccountInfo getAccountInfo(EntityId userId) {
        UserView view = getUserUseCase.getUser(new GetUserQuery(UserId.of(userId.value())));
        return new AccountInfo(EmailVO.of(view.email()), view.fullName(), view.phone(), view.verified());
    }

    @Override
    public void grantPropertyRole(EntityId userId, EntityId partyId, EntityId propertyId, String targetRole) {
        grantPropertyRoleUseCase.grant(new GrantPropertyRoleCommand(
                UserId.of(userId.value()), partyId, propertyId, PropertyRole.valueOf(targetRole)));
    }
}
