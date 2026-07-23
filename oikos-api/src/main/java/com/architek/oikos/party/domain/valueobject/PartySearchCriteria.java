package com.architek.oikos.contact.domain.valueobject;

/**
 * Optional list-filter axis for contact search: null means "no filter on this axis".
 */
public record ContactSearchCriteria(String search) {

    public static ContactSearchCriteria empty() {
        return new ContactSearchCriteria(null);
    }
}
