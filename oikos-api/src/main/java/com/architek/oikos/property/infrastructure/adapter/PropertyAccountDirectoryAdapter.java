package com.architek.oikos.property.infrastructure.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.architek.oikos.property.application.port.out.AccountDirectoryPort;
import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindLinkedPartyInPropertyUseCase;

@Component
public class PropertyAccountDirectoryAdapter implements AccountDirectoryPort {

    private final FindLinkedPartyInPropertyUseCase findLinkedPartyInPropertyUseCase;

    public PropertyAccountDirectoryAdapter(FindLinkedPartyInPropertyUseCase findLinkedPartyInPropertyUseCase) {
        this.findLinkedPartyInPropertyUseCase = findLinkedPartyInPropertyUseCase;
    }

    @Override
    public Optional<EntityId> findLinkedPartyInProperty(EmailVO accountEmail, EntityId propertyId) {
        return findLinkedPartyInPropertyUseCase.findLinkedParty(accountEmail, propertyId);
    }
}
