package com.architek.oikos.user.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;
import com.architek.oikos.user.domain.model.RoleCategory;

public class PropertyCreationLimitExceededException extends BusinessException {

    public PropertyCreationLimitExceededException(RoleCategory category, int maxProperties) {
        super("Role category " + category + " is capped at " + maxProperties
                + " managed propert" + (maxProperties == 1 ? "y" : "ies"));
    }
}
