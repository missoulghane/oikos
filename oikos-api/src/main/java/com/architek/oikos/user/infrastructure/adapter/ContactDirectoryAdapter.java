package com.architek.oikos.user.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.contact.application.command.CreateContactCommand;
import com.architek.oikos.contact.application.command.UpdateContactCommand;
import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.port.in.CreateContactUseCase;
import com.architek.oikos.contact.application.port.in.GetContactUseCase;
import com.architek.oikos.contact.application.port.in.LoadContactIdByEmailUseCase;
import com.architek.oikos.contact.application.port.in.LoadContactIdByPhoneUseCase;
import com.architek.oikos.contact.application.port.in.UpdateContactUseCase;
import com.architek.oikos.contact.application.query.GetContactQuery;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.out.ContactDetails;
import com.architek.oikos.user.application.port.out.ContactDirectoryPort;

/**
 * Cross-feature adapter: delegates to contact's public port-in use cases
 * (CreateContactUseCase, GetContactUseCase, UpdateContactUseCase,
 * LoadContactIdByEmailUseCase, LoadContactIdByPhoneUseCase), never to contact's
 * repository directly (rule 6).
 */
@Component
public class ContactDirectoryAdapter implements ContactDirectoryPort {

    private final CreateContactUseCase createContactUseCase;
    private final GetContactUseCase getContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final LoadContactIdByEmailUseCase loadContactIdByEmailUseCase;
    private final LoadContactIdByPhoneUseCase loadContactIdByPhoneUseCase;

    public ContactDirectoryAdapter(CreateContactUseCase createContactUseCase,
                                    GetContactUseCase getContactUseCase,
                                    UpdateContactUseCase updateContactUseCase,
                                    LoadContactIdByEmailUseCase loadContactIdByEmailUseCase,
                                    LoadContactIdByPhoneUseCase loadContactIdByPhoneUseCase) {
        this.createContactUseCase = createContactUseCase;
        this.getContactUseCase = getContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
        this.loadContactIdByEmailUseCase = loadContactIdByEmailUseCase;
        this.loadContactIdByPhoneUseCase = loadContactIdByPhoneUseCase;
    }

    @Override
    public EntityId createContact(ContactDetails details) {
        ContactId id = createContactUseCase.create(new CreateContactCommand(
                details.lastName(), details.firstName(), details.email(), details.phone()));
        return EntityId.of(id.asUuid());
    }

    @Override
    public ContactDetails getContactById(EntityId contactId) {
        ContactView view = getContactUseCase.getContact(new GetContactQuery(ContactId.of(contactId.value())));
        return toDetails(view);
    }

    @Override
    public ContactDetails updateContact(EntityId contactId, ContactDetails details) {
        ContactView view = updateContactUseCase.update(new UpdateContactCommand(
                ContactId.of(contactId.value()), details.lastName(), details.firstName(), details.email(), details.phone()));
        return toDetails(view);
    }

    @Override
    public Optional<EntityId> findIdByEmail(EmailVO email) {
        return loadContactIdByEmailUseCase.loadByEmail(email).map(id -> EntityId.of(id.asUuid()));
    }

    @Override
    public Optional<EntityId> findIdByPhone(String phone) {
        return loadContactIdByPhoneUseCase.loadByPhone(phone).map(id -> EntityId.of(id.asUuid()));
    }

    private static ContactDetails toDetails(ContactView view) {
        return new ContactDetails(view.lastName(), view.firstName(), EmailVO.of(view.email()), view.phone());
    }
}
