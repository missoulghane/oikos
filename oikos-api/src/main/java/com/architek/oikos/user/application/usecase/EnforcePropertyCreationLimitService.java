package com.architek.oikos.user.application.usecase;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.architek.oikos.user.application.dto.UserAccessView;
import com.architek.oikos.user.domain.exception.PropertyCreationLimitExceededException;
import com.architek.oikos.user.domain.model.RoleCategory;

/**
 * Enforces the "how many properties may this role category create" rule
 * (e.g. a volunteer syndic - RoleCategory.BOARD - may create only one).
 * The cap is config, not code (oikos.rbac.board-tier.max-properties),
 * mirroring ConfigurePropertyService.maxUnitsPerRequest - raising it later
 * is a config change, never a redeploy of business logic.
 */
@Component
public class EnforcePropertyCreationLimitService {

    private final int boardTierMaxProperties;

    public EnforcePropertyCreationLimitService(
            @Value("${oikos.rbac.board-tier.max-properties}") int boardTierMaxProperties) {
        this.boardTierMaxProperties = boardTierMaxProperties;
    }

    public void enforce(UserAccessView access, RoleCategory category) {
        if (category != RoleCategory.BOARD) {
            return;
        }
        if (access.countAdminTierProperties() >= boardTierMaxProperties) {
            throw new PropertyCreationLimitExceededException(category, boardTierMaxProperties);
        }
    }
}
