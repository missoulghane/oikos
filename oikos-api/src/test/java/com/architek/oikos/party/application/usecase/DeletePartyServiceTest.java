package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.command.DeletePartyCommand;
import com.architek.oikos.party.domain.exception.PartyNotFoundException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class DeletePartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private DeletePartyService newService() {
        return new DeletePartyService(partyRepository);
    }

    @Test
    void deleting_an_existing_party_removes_it() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findById(id)).thenReturn(Optional.of(party));

        newService().delete(new DeletePartyCommand(id));

        verify(partyRepository).deleteById(id);
    }

    @Test
    void deleting_a_missing_party_throws() {
        PartyId id = PartyId.newId();
        when(partyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().delete(new DeletePartyCommand(id)))
                .isInstanceOf(PartyNotFoundException.class);
    }
}
