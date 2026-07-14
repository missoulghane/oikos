package com.architek.oikos.contact.web.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import com.architek.oikos.contact.application.command.CreateContactCommand;
import com.architek.oikos.contact.application.command.DeleteContactCommand;
import com.architek.oikos.contact.application.command.UpdateContactCommand;
import com.architek.oikos.contact.application.port.in.CreateContactUseCase;
import com.architek.oikos.contact.application.port.in.DeleteContactUseCase;
import com.architek.oikos.contact.application.port.in.GetContactUseCase;
import com.architek.oikos.contact.application.port.in.ListContactsUseCase;
import com.architek.oikos.contact.application.port.in.UpdateContactUseCase;
import com.architek.oikos.contact.application.query.GetContactQuery;
import com.architek.oikos.contact.application.query.ListContactsQuery;
import com.architek.oikos.contact.domain.valueobject.ContactId;
import com.architek.oikos.contact.domain.valueobject.ContactSearchCriteria;
import com.architek.oikos.contact.web.request.CreateContactRequest;
import com.architek.oikos.contact.web.request.UpdateContactRequest;
import com.architek.oikos.contact.web.response.ContactResponse;
import com.architek.oikos.contact.web.response.PagedContactResponse;
import com.architek.oikos.shared.domain.pagination.PageRequest;
import com.architek.oikos.shared.domain.valueobject.EmailVO;

/**
 * Administrative management of contacts (identity records independent of any
 * application account). Reserved to ROLE_ADMIN for now; will be revisited once
 * per-copropriete contextual access (RG-ACC-02) is introduced.
 */
@RestController
@RequestMapping("/contacts")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ContactController {

    private final CreateContactUseCase createContactUseCase;
    private final GetContactUseCase getContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final ListContactsUseCase listContactsUseCase;
    private final DeleteContactUseCase deleteContactUseCase;

    public ContactController(CreateContactUseCase createContactUseCase,
                              GetContactUseCase getContactUseCase,
                              UpdateContactUseCase updateContactUseCase,
                              ListContactsUseCase listContactsUseCase,
                              DeleteContactUseCase deleteContactUseCase) {
        this.createContactUseCase = createContactUseCase;
        this.getContactUseCase = getContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
        this.listContactsUseCase = listContactsUseCase;
        this.deleteContactUseCase = deleteContactUseCase;
    }

    @GetMapping
    public PagedContactResponse list(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      @RequestParam(required = false) String search) {
        ListContactsQuery query = new ListContactsQuery(PageRequest.of(page, size), new ContactSearchCriteria(search));
        return PagedContactResponse.from(listContactsUseCase.listContacts(query));
    }

    @GetMapping("/{id}")
    public ContactResponse getById(@PathVariable String id) {
        return ContactResponse.from(getContactUseCase.getContact(new GetContactQuery(ContactId.of(id))));
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateContactRequest request) {
        ContactId id = createContactUseCase.create(new CreateContactCommand(
                request.lastName(), request.firstName(), EmailVO.of(request.email()), request.phone()));
        return ResponseEntity.created(URI.create("/api/v1/contacts/" + id)).build();
    }

    @PutMapping("/{id}")
    public ContactResponse update(@PathVariable String id, @Valid @RequestBody UpdateContactRequest request) {
        UpdateContactCommand command = new UpdateContactCommand(
                ContactId.of(id), request.lastName(), request.firstName(), EmailVO.of(request.email()), request.phone());
        return ContactResponse.from(updateContactUseCase.update(command));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        deleteContactUseCase.delete(new DeleteContactCommand(ContactId.of(id)));
    }
}
