package com.architek.oikos.property.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.property.application.dto.LinkedContactView;
import com.architek.oikos.property.application.port.in.FindContactByAccountEmailUseCase;
import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
import com.architek.oikos.property.application.port.out.PartyDetails;
import com.architek.oikos.property.application.port.out.PartyDirectoryPort;
import com.architek.oikos.property.application.query.FindContactByAccountEmailQuery;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Component
public class FindContactByAccountEmailService implements FindContactByAccountEmailUseCase {

    private final AccountDirectoryPort accountDirectoryPort;
    private final PartyDirectoryPort partyDirectoryPort;

    public FindContactByAccountEmailService(AccountDirectoryPort accountDirectoryPort,
                                             PartyDirectoryPort partyDirectoryPort) {
        this.accountDirectoryPort = accountDirectoryPort;
        this.partyDirectoryPort = partyDirectoryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LinkedContactView> findContact(FindContactByAccountEmailQuery query) {
        EntityId propertyId = query.propertyId().value();
        return accountDirectoryPort.findLinkedPartyInProperty(query.accountEmail(), propertyId)
                .map(partyId -> {
                    PartyDetails details = partyDirectoryPort.getPartyById(partyId);
                    return new LinkedContactView(partyId, details.fullName(),
                            details.email() != null ? details.email().value() : null);
                });
    }
}
