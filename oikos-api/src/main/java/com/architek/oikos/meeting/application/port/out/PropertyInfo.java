package com.architek.oikos.meeting.application.port.out;

import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * What meeting knows of a property. votingWeightMode is already translated
 * from property's DuesCalculationMode by the adapter, so nothing downstream
 * of this port has to know the two concepts are related.
 */
public record PropertyInfo(EntityId id, String name, VotingWeightMode votingWeightMode) {
}
