package com.architek.oikos.contact.application.port.in;

import com.architek.oikos.contact.application.dto.ContactView;
import com.architek.oikos.contact.application.query.GetContactQuery;

public interface GetContactUseCase {

    ContactView getContact(GetContactQuery query);
}
