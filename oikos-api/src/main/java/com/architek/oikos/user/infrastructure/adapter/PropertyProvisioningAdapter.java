package com.architek.oikos.user.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.application.command.CreatePropertyCommand;
import com.architek.oikos.property.application.port.in.AddBoardMemberUseCase;
import com.architek.oikos.property.application.port.in.CreatePropertyUseCase;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.BoardSeat;
import com.architek.oikos.user.application.port.out.PropertyProvisioningDetails;
import com.architek.oikos.user.application.port.out.PropertyProvisioningPort;

/**
 * Cross-feature adapter: delegates to property's public port-in use cases
 * (CreatePropertyUseCase, AddBoardMemberUseCase), never to property's
 * repositories directly (rule 6). Seats the newly registered account's Party
 * on the property's board.
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
                details.name(), details.address(), details.city()));
        return EntityId.of(propertyId.asUuid());
    }

    @Override
    public void assignPropertyManager(EntityId propertyId, EntityId partyId) {
        seatOnBoard(propertyId, partyId, BoardRole.PROPERTY_MANAGER);
    }

    @Override
    public void assignBoardMember(EntityId propertyId, EntityId partyId, BoardSeat seat) {
        seatOnBoard(propertyId, partyId, toBoardRole(seat));
    }

    private void seatOnBoard(EntityId propertyId, EntityId partyId, BoardRole boardRole) {
        addBoardMemberUseCase.add(new AddBoardMemberCommand(
                PropertyId.of(propertyId.value()), partyId, null, null, null, boardRole));
    }

    private static BoardRole toBoardRole(BoardSeat seat) {
        return switch (seat) {
            case VOLUNTEER_MANAGER -> BoardRole.VOLUNTEER_MANAGER;
            case PRESIDENT -> BoardRole.PRESIDENT;
            case TREASURER -> BoardRole.TREASURER;
            case SECRETARY -> BoardRole.SECRETARY;
            case MEMBER -> BoardRole.MEMBER;
        };
    }
}
