package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision the accounting account mirroring a property's
 * activity as soon as the property exists. Implemented in
 * property.infrastructure.adapter by delegating to accounting's public port-in
 * use case, never to accounting's repository directly (rule 6). The generic
 * {@link EntityId} keeps property.application fully decoupled from accounting's
 * own AccountId/AccountType types.
 */
public interface PropertyAccountProvisioningPort {

    void provisionAccount(EntityId propertyId);
}
