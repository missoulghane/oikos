package com.architek.oikos.property.domain.exception;

import com.architek.oikos.shared.exception.BusinessException;

public class PropertyConfigurationLimitExceededException extends BusinessException {

    public PropertyConfigurationLimitExceededException(int requestedUnitCount, int maxUnitCount) {
        super("Requested unit count (" + requestedUnitCount
                + ") exceeds the maximum allowed per configuration request (" + maxUnitCount + ")");
    }
}
