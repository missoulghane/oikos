package com.architek.oikos.property.application.port.in;

import com.architek.oikos.property.application.query.CountUnitsByPropertyQuery;

/**
 * How many lots a copropriété holds - the figure a syndic reads on their
 * dashboard beside its name and address.
 *
 * <p>Its own use case rather than a field on PropertyView: the count is a
 * query against another table, and every producer of that view (update, budget,
 * listing) would have to run it to fill a field most of them do not need.
 */
public interface CountUnitsByPropertyUseCase {

    long countUnits(CountUnitsByPropertyQuery query);
}
