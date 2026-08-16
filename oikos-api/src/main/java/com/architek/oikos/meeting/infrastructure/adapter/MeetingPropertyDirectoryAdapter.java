package com.architek.oikos.meeting.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.meeting.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.meeting.application.port.out.PropertyInfo;
import com.architek.oikos.meeting.domain.valueobject.VotingWeightMode;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.valueobject.DuesCalculationMode;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: goes exclusively through property's public port-in
 * use case, never its domain model, repositories or infrastructure (rule
 * 4/6). Named with the Meeting prefix like every other module's directory
 * adapter, so the Spring bean cannot collide with theirs.
 *
 * <p>This is also the one place aware that a copropriété's dues calculation
 * mode and its voting weight are the same setting seen twice - the
 * translation happens here and nothing downstream has to know (ADR 0002 §3).
 */
@Component
public class MeetingPropertyDirectoryAdapter implements PropertyDirectoryPort {

    private final GetPropertyUseCase getPropertyUseCase;

    public MeetingPropertyDirectoryAdapter(GetPropertyUseCase getPropertyUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
    }

    @Override
    public PropertyInfo getProperty(EntityId propertyId) {
        PropertyView property = getPropertyUseCase.getProperty(new GetPropertyQuery(PropertyId.of(propertyId.value())));
        return new PropertyInfo(propertyId, property.name(), toVotingWeightMode(property.duesCalculationMode()));
    }

    private static VotingWeightMode toVotingWeightMode(DuesCalculationMode duesCalculationMode) {
        return duesCalculationMode == DuesCalculationMode.SHARES ? VotingWeightMode.SHARES : VotingWeightMode.PER_UNIT;
    }
}
