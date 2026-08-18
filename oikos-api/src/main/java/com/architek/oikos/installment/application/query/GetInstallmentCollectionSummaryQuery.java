package com.architek.oikos.installment.application.query;

import java.time.LocalDate;

import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * asOf is passed in rather than read from a clock down here, exactly as the
 * listing does with its "à échoir" cutoff: "today" is a request-time notion,
 * and keeping it out of the lower layers leaves them testable against a fixed
 * date.
 */
public record GetInstallmentCollectionSummaryQuery(EntityId propertyId, LocalDate asOf) {
}
