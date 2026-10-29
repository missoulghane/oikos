package com.architek.oikos.installment.application.event;

import java.util.List;

import com.architek.oikos.installment.domain.valueobject.InstallmentCallId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Published when a fund call is committed, with the units it charged - the
 * advance regularization listener tries each of them, so that an advance a lot
 * already holds is imputed as soon as the new call is due, without waiting for
 * the lot's next payment.
 */
public record InstallmentCallGeneratedEvent(InstallmentCallId installmentCallId, EntityId propertyId,
                                            List<EntityId> chargedUnitIds, EntityId generatedByUserId) {
}
