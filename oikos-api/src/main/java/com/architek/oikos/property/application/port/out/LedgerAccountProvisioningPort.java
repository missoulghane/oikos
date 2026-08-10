package com.architek.oikos.property.application.port.out;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Outbound port used to provision the PCM ledger accounts of a property
 * (its cash account) and of a unit (its dedicated receivable account) as
 * soon as each is created ("exigence supplementaire", ADR 0001). Implemented
 * in property.infrastructure.adapter by delegating to accounting's public
 * port-in use cases, never to accounting's repository directly (rule 4/6).
 */
public interface LedgerAccountProvisioningPort {

    void provisionPropertyCashAccount(EntityId propertyId);

    void provisionUnitReceivableAccount(EntityId propertyId, EntityId unitId);
}
