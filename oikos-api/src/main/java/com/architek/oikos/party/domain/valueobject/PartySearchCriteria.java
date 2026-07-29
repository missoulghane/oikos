package com.architek.oikos.party.domain.valueobject;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Party search is always scoped to one property (mandatory, since a Party
 * has no meaning outside the property it belongs to); search is an optional
 * additional filter axis: null means "no filter on this axis".
 */
public record PartySearchCriteria(EntityId propertyId, String search) {

    public static PartySearchCriteria of(EntityId propertyId) {
        return new PartySearchCriteria(propertyId, null);
    }
}
