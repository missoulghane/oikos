package com.architek.oikos.invitation.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.invitation.application.port.out.PropertyBasicInfo;
import com.architek.oikos.invitation.application.port.out.PropertyDirectoryPort;
import com.architek.oikos.property.application.dto.PropertyView;
import com.architek.oikos.property.application.port.in.GetPropertyUseCase;
import com.architek.oikos.property.application.query.GetPropertyQuery;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;

/**
 * Cross-feature adapter: delegates to property's public port-in
 * (GetPropertyUseCase), never to property's repository directly (rule 6).
 */
@Component
public class InvitationPropertyDirectoryAdapter implements PropertyDirectoryPort {

    private final GetPropertyUseCase getPropertyUseCase;

    public InvitationPropertyDirectoryAdapter(GetPropertyUseCase getPropertyUseCase) {
        this.getPropertyUseCase = getPropertyUseCase;
    }

    @Override
    public Optional<PropertyBasicInfo> findBasicInfo(EntityId propertyId) {
        try {
            PropertyView view = getPropertyUseCase.getProperty(new GetPropertyQuery(new PropertyId(propertyId)));
            return Optional.of(new PropertyBasicInfo(view.name(), view.address()));
        } catch (PropertyNotFoundException e) {
            return Optional.empty();
        }
    }
}
