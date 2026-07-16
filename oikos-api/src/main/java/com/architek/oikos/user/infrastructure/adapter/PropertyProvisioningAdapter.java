package com.architek.oikos.user.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;

/**
 * Cross-feature adapter: delegates to property's public port-in use cases
 * (CreatePropertyUseCase, AddBoardMemberUseCase), never to property's
 * repositories directly (rule 6). Assigns the newly registered user as the
 * property's PROPERTY_MANAGER board member.
 */
@Component
public class PropertyProvisioningAdapter implements PropertyProvisioningPort {

    private final CreatePropertyUseCase createPropertyUseCase;
    private final AddBoardMemberUseCase addBoardMemberUseCase;

    public PropertyProvisioningAdapter(CreatePropertyUseCase createPropertyUseCase,
                                        AddBoardMemberUseCase addBoardMemberUseCase) {
        this.createPropertyUseCase = createPropertyUseCase;
        this.addBoardMemberUseCase = addBoardMemberUseCase;
    }

    @Override
    public EntityId provisionProperty(PropertyProvisioningDetails details) {
        PropertyId propertyId = createPropertyUseCase.create(new CreatePropertyCommand(
                details.name(), details.address(), null, null));

        addBoardMemberUseCase.add(new AddBoardMemberCommand(propertyId, details.managerContactId(), BoardRole.PROPERTY_MANAGER));

        return EntityId.of(propertyId.asUuid());
    }
}
