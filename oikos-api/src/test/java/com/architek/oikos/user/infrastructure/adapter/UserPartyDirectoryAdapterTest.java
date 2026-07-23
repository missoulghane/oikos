package com.architek.oikos.user.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.party.application.dto.PartyView;
import com.architek.oikos.party.application.port.in.CreatePartyUseCase;
import com.architek.oikos.party.application.port.in.GetPartyUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByEmailUseCase;
import com.architek.oikos.party.application.port.in.LoadPartyIdByPhoneUseCase;
import com.architek.oikos.party.application.port.in.UpdatePartyUseCase;
import com.architek.oikos.party.domain.valueobject.PartyId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.PartyType;
import com.architek.oikos.user.application.port.out.PartyDetails;

@ExtendWith(MockitoExtension.class)
class UserPartyDirectoryAdapterTest {

    @Mock
    private CreatePartyUseCase createPartyUseCase;

    @Mock
    private GetPartyUseCase getPartyUseCase;

    @Mock
    private UpdatePartyUseCase updatePartyUseCase;

    @Mock
    private LoadPartyIdByEmailUseCase loadPartyIdByEmailUseCase;

    @Mock
    private LoadPartyIdByPhoneUseCase loadPartyIdByPhoneUseCase;

    private UserPartyDirectoryAdapter newAdapter() {
        return new UserPartyDirectoryAdapter(createPartyUseCase, getPartyUseCase, updatePartyUseCase,
                loadPartyIdByEmailUseCase, loadPartyIdByPhoneUseCase);
    }

    @Test
    void createParty_delegates_to_parties_create_use_case() {
        PartyId partyId = PartyId.newId();
        when(createPartyUseCase.create(any())).thenReturn(partyId);

        var result = newAdapter().createParty(new PartyDetails("Jane Doe", EmailVO.of("jane@doe.com"), null));

        assertThat(result.value()).isEqualTo(partyId.asUuid());
    }

    @Test
    void getPartyById_maps_the_party_view_to_party_details() {
        PartyId partyId = PartyId.newId();
        when(getPartyUseCase.getParty(any()))
                .thenReturn(new PartyView(partyId, "Jane Doe", PartyType.INDIVIDUAL, "jane@doe.com", "0600000000"));

        PartyDetails details = newAdapter().getPartyById(com.architek.oikos.shared.domain.valueobject.EntityId.of(partyId.asUuid()));

        assertThat(details.fullName()).isEqualTo("Jane Doe");
        assertThat(details.email()).isEqualTo(EmailVO.of("jane@doe.com"));
        assertThat(details.phone()).isEqualTo("0600000000");
    }

    @Test
    void findIdByEmail_and_findIdByPhone_return_empty_when_party_does_not_exist() {
        when(loadPartyIdByEmailUseCase.loadByEmail(any())).thenReturn(Optional.empty());
        when(loadPartyIdByPhoneUseCase.loadByPhone(any())).thenReturn(Optional.empty());

        UserPartyDirectoryAdapter adapter = newAdapter();

        assertThat(adapter.findIdByEmail(EmailVO.of("nobody@doe.com"))).isEmpty();
        assertThat(adapter.findIdByPhone("0000000000")).isEmpty();
    }
}
