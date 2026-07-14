package com.architek.oikos.contact.application.port.in;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.query.ListContactsQuery;
import com.architek.oikos.shared.domain.pagination.Page;

public interface ListContactsUseCase {

    Page<ContactView> listContacts(ListContactsQuery query);
}
