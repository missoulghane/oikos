package com.architek.oikos.party.domain.valueobject;

/**
 * Optional list-filter axis for party search: null means "no filter on this axis".
 */
public record PartySearchCriteria(String search) {

    public static PartySearchCriteria empty() {
        return new PartySearchCriteria(null);
    }
}
