package com.architek.oikos.party.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.command.CreatePartyCommand;
import com.architek.oikos.party.domain.exception.EmailAlreadyUsedException;
import com.architek.oikos.party.domain.model.Party;
import com.architek.oikos.party.domain.repository.PartyRepository;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

@ExtendWith(MockitoExtension.class)
class CreatePartyServiceTest {

    @Mock
    private PartyRepository partyRepository;

    private CreatePartyService newService() {
        return new CreatePartyService(partyRepository);
    }

    @Test
    void creating_a_party_persists_it() {
        when(partyRepository.existsByEmail(any())).thenReturn(false);
        when(partyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CreatePartyCommand command = new CreatePartyCommand("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);

        newService().create(command);

        ArgumentCaptor<Party> captor = ArgumentCaptor.forClass(Party.class);
        verify(partyRepository).save(captor.capture());
        assertThat(captor.getValue().getFullName()).isEqualTo("Jane Doe");
        assertThat(captor.getValue().getPartyType()).isEqualTo(PartyType.INDIVIDUAL);
    }

    @Test
    void creating_a_party_with_an_already_used_email_is_rejected() {
        when(partyRepository.existsByEmail(any())).thenReturn(true);

        CreatePartyCommand command = new CreatePartyCommand("Jane Doe", PartyType.INDIVIDUAL, EmailVO.of("jane@doe.com"), null);

        assertThatThrownBy(() -> newService().create(command)).isInstanceOf(EmailAlreadyUsedException.class);
    }
}
