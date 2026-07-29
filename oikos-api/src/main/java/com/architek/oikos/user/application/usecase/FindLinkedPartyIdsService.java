package com.architek.oikos.user.application.usecase;

import java.util.Collection;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.shared.domain.valueobject.EntityId;
import com.architek.oikos.user.application.port.in.FindLinkedPartyIdsUseCase;
import com.architek.oikos.user.domain.repository.UserRepository;

@Component
public class FindLinkedPartyIdsService implements FindLinkedPartyIdsUseCase {

    private final UserRepository userRepository;

    public FindLinkedPartyIdsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<EntityId> findLinkedPartyIds(Collection<EntityId> partyIds) {
        return userRepository.findLinkedPartyIds(partyIds);
    }
}
