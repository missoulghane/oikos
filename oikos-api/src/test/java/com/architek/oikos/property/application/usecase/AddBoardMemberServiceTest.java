package com.architek.oikos.property.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.property.application.command.AddBoardMemberCommand;
import com.architek.oikos.property.domain.exception.PartyAlreadyHasRoleException;
import com.architek.oikos.property.domain.exception.PropertyNotFoundException;
import com.architek.oikos.property.domain.model.Property;
import com.architek.oikos.property.domain.repository.PropertyRepository;
import com.architek.oikos.property.domain.repository.BoardMemberRepository;
import com.architek.oikos.property.domain.valueobject.PropertyId;
import com.architek.oikos.property.domain.valueobject.BoardRole;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@ExtendWith(MockitoExtension.class)
class AddBoardMemberServiceTest {

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private PropertyRepository propertyRepository;

    private AddBoardMemberService newService() {
        return new AddBoardMemberService(boardMemberRepository, propertyRepository);
    }

    @Test
    void adding_a_board_member_to_an_existing_property_persists_it() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(false);
        when(boardMemberRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        newService().add(new AddBoardMemberCommand(propertyId, EntityId.newId(), BoardRole.PRESIDENT));
    }

    @Test
    void adding_the_same_role_twice_for_the_same_party_and_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId))
                .thenReturn(Optional.of(Property.create(propertyId, "Copro", "Address")));
        when(boardMemberRepository.existsByPropertyIdAndPartyIdAndBoardRole(any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> newService().add(new AddBoardMemberCommand(propertyId, EntityId.newId(), BoardRole.PRESIDENT)))
                .isInstanceOf(PartyAlreadyHasRoleException.class);
    }

    @Test
    void adding_a_board_member_to_an_unknown_property_is_rejected() {
        PropertyId propertyId = PropertyId.newId();
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().add(new AddBoardMemberCommand(propertyId, EntityId.newId(), BoardRole.PRESIDENT)))
                .isInstanceOf(PropertyNotFoundException.class);
    }
}
