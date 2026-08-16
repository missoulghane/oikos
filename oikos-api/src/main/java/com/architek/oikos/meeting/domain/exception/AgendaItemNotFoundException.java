package com.architek.oikos.meeting.domain.exception;

import com.architek.oikos.meeting.domain.valueobject.AgendaItemId;
import com.architek.oikos.shared.exception.ResourceNotFoundException;

public class AgendaItemNotFoundException extends ResourceNotFoundException {

    public AgendaItemNotFoundException(AgendaItemId id) {
        super("Agenda item not found with id: " + id);
    }
}
