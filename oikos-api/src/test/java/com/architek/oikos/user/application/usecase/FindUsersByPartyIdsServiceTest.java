package com.architek.oikos.user.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.HashedPassword;
import com.architek.oikos.user.domain.model.User;
import com.architek.oikos.user.domain.repository.UserRepository;
import com.architek.oikos.user.domain.valueobject.UserId;

@ExtendWith(MockitoExtension.class)
class FindUsersByPartyIdsServiceTest {

    @Mock
    private UserRepository userRepository;

    private FindUsersByPartyIdsService newService() {
        return new FindUsersByPartyIdsService(userRepository);
    }

    @Test
    void maps_each_requested_party_id_to_its_owning_user_id() {
        EntityId partyA = EntityId.newId();
        EntityId partyB = EntityId.newId();
        EntityId unrelatedParty = EntityId.newId();

        User userWithBothParties = User.register(UserId.newId(), EmailVO.of("a@oikos.com"), "A", HashedPassword.of("hash"))
                .withLinkedParty(partyA).withLinkedParty(unrelatedParty);
        User userWithPartyB = User.register(UserId.newId(), EmailVO.of("b@oikos.com"), "B", HashedPassword.of("hash"))
                .withLinkedParty(partyB);

        when(userRepository.findByLinkedPartyIds(List.of(partyA, partyB))).thenReturn(List.of(userWithBothParties, userWithPartyB));

        Map<EntityId, EntityId> result = newService().findUserIdsByPartyIds(List.of(partyA, partyB));

        assertThat(result).containsOnly(
                Map.entry(partyA, userWithBothParties.getId().value()),
                Map.entry(partyB, userWithPartyB.getId().value()));
    }

    @Test
    void returns_an_empty_map_when_no_user_matches() {
        when(userRepository.findByLinkedPartyIds(List.of())).thenReturn(List.of());

        assertThat(newService().findUserIdsByPartyIds(List.of())).isEmpty();
    }
}
