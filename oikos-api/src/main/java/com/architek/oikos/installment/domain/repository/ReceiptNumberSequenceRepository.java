package com.architek.oikos.installment.domain.repository;

import com.architek.oikos.installment.domain.valueobject.ReceiptNumber;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Allocates the next receipt number of a (property, year) series. Separate from
 * PaymentRepository because it is a counter, not payment storage - the same
 * split accounting makes between JournalEntryRepository and its piece sequence.
 */
public interface ReceiptNumberSequenceRepository {

    ReceiptNumber allocate(EntityId propertyId, int year);
}
