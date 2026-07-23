package com.architek.oikos.contact.web.response;

import com.architek.oikos.contact.application.dto.ContactView;

public record ContactResponse(String id, String lastName, String firstName, String email, String phone) {

    public static ContactResponse from(ContactView view) {
        return new ContactResponse(view.id().toString(), view.lastName(), view.firstName(), view.email(), view.phone());
    }
}
