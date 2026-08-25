package com.architek.oikos.user.application.usecase;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EmailVO;
import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindLinkedPartyInPropertyUseCase;
import com.architek.oikos.user.domain.model.PropertyRoleGrant;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class FindLinkedPartyInPropertyService implements FindLinkedPartyInPropertyUseCase {

    private final UserRepository userRepository;

    public FindLinkedPartyInPropertyService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EntityId> findLinkedParty(EmailVO accountEmail, EntityId propertyId) {
        return userRepository.findByEmail(accountEmail.value())
                .flatMap(user -> user.getPropertyRoleGrants().stream()
                        .filter(grant -> grant.propertyId().equals(propertyId))
                        .map(PropertyRoleGrant::partyId)
                        .findFirst());
    }
}
