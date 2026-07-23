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
class LoadPartyIdByEmailServiceTest {

    @Mock
    private PartyRepository partyRepository;

    @Test
    void resolves_the_party_id_for_a_known_email() {
        PartyId id = PartyId.newId();
        Party party = Party.create(id, "Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);
        when(partyRepository.findByEmail(EmailVO.of("jane@doe.com"))).thenReturn(Optional.of(party));

        var result = new LoadPartyIdByEmailService(partyRepository).loadByEmail(EmailVO.of("jane@doe.com"));

        assertThat(result).contains(id);
    }

    @Test
    void returns_empty_for_an_unknown_email() {
        when(partyRepository.findByEmail(EmailVO.of("nobody@doe.com"))).thenReturn(Optional.empty());

        var result = new LoadPartyIdByEmailService(partyRepository).loadByEmail(EmailVO.of("nobody@doe.com"));

        assertThat(result).isEmpty();
    }
}
