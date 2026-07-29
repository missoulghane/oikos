package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.query.GetPartyQuery;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class GetPartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private GetPartyService newService() {
        return new GetPartyService(partyRepository);
    }

    @Test
    void getting_an_existing_party_returns_its_view() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, EntityId.newId(), "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));

        var view = newService().getParty(new GetPartyQuery(id));

        assertThat(view.fullName()).isEqualTo("Jane Doe");
        assertThat(view.email()).isEqualTo("jane@doe.com");
    }

    @Test
    void getting_a_missing_party_throws() {
        PartyId id = PartyId.newId();
        when(partyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getParty(new GetPartyQuery(id)))
                .isInstanceOf(PartyNotFoundException.class);
    }
}
