package com.architek.oikos.user.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.CreateContactUseCase;
import com.architek.oikos.contact.application.port.in.GetContactUseCase;
import com.architek.oikos.contact.application.port.in.LoadContactIdByEmailUseCase;
import com.architek.oikos.contact.application.port.in.LoadContactIdByPhoneUseCase;
import com.architek.oikos.contact.application.port.in.UpdateContactUseCase;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.user.application.port.out.ContactDetails;

@ExtendWith(MockitoExtension.class)
class ContactDirectoryAdapterTest {

    @Mock
    private CreateContactUseCase createContactUseCase;

    @Mock
    private GetContactUseCase getContactUseCase;

    @Mock
    private UpdateContactUseCase updateContactUseCase;

    @Mock
    private LoadContactIdByEmailUseCase loadContactIdByEmailUseCase;

    @Mock
    private LoadContactIdByPhoneUseCase loadContactIdByPhoneUseCase;

    private ContactDirectoryAdapter newAdapter() {
        return new ContactDirectoryAdapter(createContactUseCase, getContactUseCase, updateContactUseCase,
                loadContactIdByEmailUseCase, loadContactIdByPhoneUseCase);
    }

    @Test
    void createContact_delegates_to_contacts_create_use_case() {
        ContactId contactId = ContactId.newId();
        when(createContactUseCase.create(any())).thenReturn(contactId);

        var result = newAdapter().createContact(new ContactDetails("Doe", "Jane", EmailVO.of("jane@doe.com"), null));

        assertThat(result.value()).isEqualTo(contactId.asUuid());
    }

    @Test
    void getContactById_maps_the_contact_view_to_contact_details() {
        ContactId contactId = ContactId.newId();
        when(getContactUseCase.getContact(any()))
                .thenReturn(new ContactView(contactId, "Doe", "Jane", "jane@doe.com", "0600000000"));

        ContactDetails details = newAdapter().getContactById(com.architek.oikos.shared.domain.valueobject.EntityId.of(contactId.asUuid()));

        assertThat(details.lastName()).isEqualTo("Doe");
        assertThat(details.email()).isEqualTo(EmailVO.of("jane@doe.com"));
        assertThat(details.phone()).isEqualTo("0600000000");
    }

    @Test
    void findIdByEmail_and_findIdByPhone_return_empty_when_contact_does_not_exist() {
        when(loadContactIdByEmailUseCase.loadByEmail(any())).thenReturn(Optional.empty());
        when(loadContactIdByPhoneUseCase.loadByPhone(any())).thenReturn(Optional.empty());

        ContactDirectoryAdapter adapter = newAdapter();

        assertThat(adapter.findIdByEmail(EmailVO.of("nobody@doe.com"))).isEmpty();
        assertThat(adapter.findIdByPhone("0000000000")).isEmpty();
    }
}
