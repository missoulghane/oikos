package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class LoadPartyIdByPhoneServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Test
    void resolves_the_party_id_for_a_known_phone_number() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), "0600000000");
        when(partyRepository.findByPhone("0600000000")).thenReturn(Optional.of(party));

        var result = new LoadPartyIdByPhoneService(partyRepository).loadByPhone("0600000000");

        assertThat(result).contains(id);
    }

    @Test
    void returns_empty_for_an_unknown_phone_number() {
        when(partyRepository.findByPhone("0700000000")).thenReturn(Optional.empty());

        var result = new LoadPartyIdByPhoneService(partyRepository).loadByPhone("0700000000");

        assertThat(result).isEmpty();
    }
}
